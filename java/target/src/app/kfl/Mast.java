package kfl;

/**
*	Hauptprogramm fuer Maststation.
*/

public class Mast {

	private static final int VER_MAJ = 1;
	private static final int VER_MIN = 3;

	private static int maxTime;

	public static int state;		// DIE Statevariable
	public static int lastErr;		// last error (in ERR state BUT also in RDY state)

	private static int lastMsgCnt;
	private static final int MSG_TIMEOUT = 200;		// 1 second

	public static void main(String[] args) {

		state = BBSys.MS_RESET;
		lastErr = 0;
		maxTime = 0;
		lastMsgCnt = 0;
		Timer.init();			// wd
		Flash.init();
		Triac.init();
		int addr = Flash.getStationAddress();
		Msg.init(addr);
		init(addr);

		JopSys.wr(0x02, BBSys.IO_LED);	// Betrieb
		Msg.flush();					// remove pending messages in hw buffer

		Timer.start();

		forever();
	}

	private static void init(int addr) {

		// nothing to do
	}

/**
*	die wichtigsten (zeitkritischen) Cmds, die in jedem State gelten
*/
	static boolean handleMsg(int val) {

		int data;

		if (val==BBSys.CMD_STATUS) {
			Msg.write(state);
			return true;
		}

		if (val==BBSys.CMD_STOP) {
			state = BBSys.MS_RDY;
			Triac.stop();
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_UP) {
			if (state==BBSys.MS_RDY) {
				state = BBSys.MS_UP;
				Triac.rauf();
				Msg.write(0);
			} else {
				Msg.err(1);
			}
			return true;
		}
		if (val==BBSys.CMD_DOWN) {
			if (state==BBSys.MS_RDY) {
				state = BBSys.MS_DOWN;
				Triac.runter();
				Msg.write(0);
			} else {
				Msg.err(1);
			}
			return true;
		}
		if (val==BBSys.CMD_PAUSE) {
			if (state==BBSys.MS_UP || state==BBSys.MS_DOWN) {
				Triac.pause();
				Msg.write(0);
			} else {
				Msg.err(1);
			}
			return true;
		}

		if (val==BBSys.CMD_INP) {
			Msg.write((JopSys.rd(BBSys.IO_SENSOR)<<4) + JopSys.rd(BBSys.IO_TAST));
			return true;
		}
		if (val==BBSys.CMD_OPTO) {
			Msg.write(Triac.getOpto());
			return true;
		}
		if (val==BBSys.CMD_CNT) {
			Msg.write(Triac.getCnt());
			return true;

		}
		return false;
	}


/**
*	allg. cmd's, nicht so zeitkritisch
*/
	static boolean handleRest(int val) {

		int data;

		if (val==BBSys.CMD_SET_STATE) {
			if (state!=BBSys.MS_UP && state!=BBSys.MS_DOWN) {	// nicht in UP/DOWN moeglich!!!
				data = Msg.readData();		// Vorsicht: Nur rdy, dbg und service sinnvoll
				if (data==BBSys.MS_RDY || data==BBSys.MS_DBG || data==BBSys.MS_SERVICE) {
					state = data;
					Msg.write(0);
				} else {
					Msg.err(2);
				}
				return true;
			} else {
				Msg.err(1);
				return true;
			}
		}
		if (val==BBSys.CMD_SETCNT) {
			Triac.setCnt(Msg.readData());
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_TIME) {
			Msg.write(maxTime>>>8);			// div. by 256 => 34.7 us per tick
			return true;
		}
		if (val==BBSys.CMD_RESTIM) {
			maxTime = 0;
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_VERSION) {
			Msg.write((VER_MAJ<<6)+VER_MIN);
			return true;

		}
		if (val==BBSys.CMD_SERVICECNT) {
			Msg.write(Triac.serviceCnt);
			return true;
		}
		if (val==BBSys.CMD_SET_DOWNCNT) {
			Triac.setDownCnt(Msg.readData());
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_SET_UPCNT) {
			Triac.setUpCnt(Msg.readData());
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_SET_MAXCNT) {
			Triac.setMaxCnt(Msg.readData());
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_ERRNR) {
			Msg.write(lastErr);
			lastErr = 0;
			state = BBSys.MS_RDY;
			return true;
		}
		if (val==BBSys.CMD_TEMP) {
			doTemp();
			return true;
		}
if (val==BBSys.CMD_DBG_DATA) {
	xxxDbgData();
	return true;
}
		if (state==BBSys.MS_DBG) {
			return handleDbg(val);
		}
		return false;
	}

private static void xxxDbgData() {

	int data = Msg.readData();
	Msg.write(Triac.getIadc(data));		// read Iadc (0..2)
}

	private static void doTemp() {

		int data = (46000-JopSys.rd(BBSys.IO_ADC))-17000+4;		// 4 for rounding
		if (data<0) data = 0;
		data >>>= 3;			// only 12 Bit
		Msg.write(data);
	}

/**
*	cmd's im state dbg.
*/
	static boolean handleDbg(int val) {

		int data;

		if (val==BBSys.CMD_SETAD) {
			data = Msg.readData();
			Flash.setStationAddress(data);
			Msg.write(0);					// answer with old address
			Msg.setAddr(data);
			return true;
		}
		if (val==BBSys.CMD_FL_PAGE) {
			data = Msg.readData();
			Flash.setPage(data);
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_FL_DATA) {
			data = Msg.readData();
			Flash.setData(data);
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_FL_PROG) {
			Flash.program();
			Msg.write(0);
			return true;
		}
		if (val==BBSys.CMD_FL_READ) {
			Msg.write(Flash.read());
			return true;
		}
		if (val==BBSys.CMD_RESET) {
			Msg.write(0);
			for(;;)
				;				// wait for WD

		}
		return false;
	}



//
//	Service loop:
//		local control of triacs.
//		never return => switch power off to resume from service
//
	private static void doService() {

		JopSys.wr(0x01, BBSys.IO_LED);

		for (;;) {
			Msg.loop();					// only for replay to set state
			Triac.loop();

			int val = JopSys.rd(BBSys.IO_TAST);
			if (val == BBSys.BIT_TAB) {
				Triac.runter();
			} else if (val == BBSys.BIT_TAUF) {
				Triac.rauf();
			} else {
				Triac.stop();
			}
			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void chkMsgTimeout() {

		if (state==BBSys.MS_UP || state==BBSys.MS_DOWN) {
			++lastMsgCnt;
			if (lastMsgCnt>MSG_TIMEOUT) {
				Triac.stop();
				lastErr = Err.MS_NO_ZS;
				state = BBSys.MS_ERR;
			}
		} else {
			lastMsgCnt = 0;
		}
	}

/**
*	main loop.
*/
	private static void forever() {

		int blinkCnt = 0;
		int cmd;

		for (;;) {

			Msg.loop();					// for exact msg timing first entry
			Triac.loop();				// should be also exact, but 1ms jitter is ok
// TODO measure jitter with osci

			if (Msg.available) {
				cmd = Msg.readCmd();
				if (!handleMsg(cmd)) {
					handleRest(cmd);
					if (state == BBSys.MS_SERVICE) {
						doService();		// never return!
					}
				}
				lastMsgCnt = 0;
			} else {
				chkMsgTimeout();
			}

			if (blinkCnt==100) {
				Timer.wd();
				blinkCnt = 0;
			}
			++blinkCnt;

			int used = Timer.usedTime();
			if (maxTime<used) maxTime = used;
			Timer.waitForNextInterval();
		}
	}
}
