--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--	confacx.vhd
--
--	configuration ACEX from ROM in PPA mode
--	version for NEW jopcore.brd (no cs/rd in, 20 MHz) with A17
--		and cut (pin 5) in jopcore.brd 2002/04
--	
--	resources on MAX7032
--
--		32 LCs !!!
--
--	timing for ACEX:
--		nConfig low							min 2 us
--		nConfig high to nStatus high		max 4 us
--		nConfig high to nWS rising edge 	max 5 us
--		nWS pulse width						min 200 ns
--		nStatus high to first rising DCLK	min 1 us
--		DCLK clk							max 33.3 MHz
--
--	for simpler config wait tbusy+trdy2ws+tws2b befor next byte
--		1.6 us + 50 ns + 50 ns
--
--
--	todo:
--		nothing
--
--	2001-10-26	creation
--	2002-01-11	changed clock div to 32 for 7.3 MHz
--	2002-05-29	ignore nce_in, noe_in => noe, nce are 'Z'
--	2002-05-30	A17 (for 1k100) on pin 5
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.std_logic_unsigned.all;

library EXEMPLAR;					-- for pin_number
use EXEMPLAR.EXEMPLAR_1164.ALL;

entity confacx is

port (
	clk		: in std_logic;
	nreset	: in std_logic;

	a		: out std_logic_vector(17 downto 0);	-- FLASH adr
	noe		: out std_logic;						-- output to FLASH
	nce		: out std_logic;						-- output to FLASH
	d0in	: in std_logic;							-- D0 from FLASH
	d0out	: out std_logic;						-- reseved DATA0 to ACEX

	nconf	: out std_logic;						-- ACEX nConfig
	conf_done	: in std_logic;						-- ACEX conf_done

	csacx	: out std_logic;						-- ACEX CS ???
	nws		: out std_logic;						-- ACEX nWS

	resacx	: out std_logic							-- ACEX reset line

);
attribute pin_number of clk 	: signal is "37";
attribute pin_number of nreset 	: signal is "43";
attribute array_pin_number of a 	: signal is (
	"5", "18", "35", "34", "33", "31", "30", "28", "19",
	"21", "22", "25", "27", "23", "20", "15", "8", "14"
);
attribute pin_number of noe 	: signal is "44";
attribute pin_number of nce 	: signal is "12";
attribute pin_number of d0in 	: signal is "2";
attribute pin_number of d0out 	: signal is "13";
attribute pin_number of nconf 	: signal is "6";
attribute pin_number of conf_done 	: signal is "38";
attribute pin_number of csacx 	: signal is "10";
attribute pin_number of nws 	: signal is "11";
attribute pin_number of resacx 	: signal is "42";

end confacx ;

architecture rtl of confacx is

	signal slowclk		: std_logic;
	signal div			: std_logic_vector(6 downto 0);

	signal state 		: std_logic_vector(4 downto 0);

	signal ar			: std_logic_vector(17 downto 0);	-- adress register

-- 
--	special encoding to use as output!
--
constant start 			:std_logic_vector(4 downto 0) := "00110";
constant wait_nCfg_2us	:std_logic_vector(4 downto 0) := "10110";
constant wait_5us		:std_logic_vector(4 downto 0) := "01111";
constant wslow			:std_logic_vector(4 downto 0) := "01101";
constant wshigh			:std_logic_vector(4 downto 0) := "11111";
constant resacex		:std_logic_vector(4 downto 0) := "00111";
constant running		:std_logic_vector(4 downto 0) := "00011";

begin

--
--	divide clock to max 250 kHz (4us for nstatus)
--
process(clk, nreset)
begin

	if nreset='0' then
		div <= (others => '0');
	else
		if rising_edge(clk) then
			div <= div + 1;
		end if;
	end if;
end process;

	slowclk <= div(5);		-- for 24 MHz
--	slowclk <= div(4);		-- for 7.3 MHz

	nconf <= state(0);
	nws <= state(1);
	resacx <= state(2);
	csacx <= state(3);
	
	
--
--	state machine
--
process(slowclk, nreset)

begin

	if nreset='0' then

		state <= start;
		ar <= (others => '0');

	else
		if rising_edge(slowclk) then
	
			case state is
	
				when start =>
--					ar <= (others => '0');
					state <= wait_nCfg_2us;
	
				when wait_nCfg_2us =>
					state <= wait_5us;
					
				when wait_5us =>
					state <= wslow;
	
				when wslow =>
					state <= wshigh;

				when wshigh =>
					ar <= ar + 1;
					if conf_done='1' then
						state <= resacex;
					else
						state <= wslow;
					end if;
	
				when resacex =>
					state <= running;

				when running =>

				when others =>
					
			end case;
		end if;
	end if;

end process;

process (state(2), ar, d0in)
begin

	if state(2)='0' then		-- is resacx
		a <= (others => 'Z');
		d0out <= '1';
		noe <= 'Z';
		nce <= 'Z';
	else
--		a <= ar;				-- use this for ACEX 1k100
		a(16 downto 0) <= ar(16 downto 0);
		a(17) <= 'Z';
		d0out <= d0in;
		noe <= '0';
		nce <= '0';
	end if;

end process;


end rtl;
