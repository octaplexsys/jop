/**
*	Definition der Konstanten fuer BB Projekt.
*	IO ports, Bits, CMDs,...
*/

public class BBSys {

	public static final int BIT_TR_ON = 0x01;
	public static final int BIT_TR_DOWN = 0x02;

//	public static final int IO_PORT = 0;	not used
	public static final int IO_ADC = 4;
	public static final int IO_WD = 7;
	public static final int IO_LED = 8;
	public static final int IO_TAST = 8;
	public static final int IO_RELAIS = 9;
	public static final int IO_SENSOR = 9;
	public static final int IO_TRIAC = 14;

	public static final int BIT_TAB = 0x01;
	public static final int BIT_TAUF = 0x02;
	public static final int BIT_TNOT = 0x04;
	public static final int BIT_TUER = 0x08;

	public static final int BIT_SENSI = 0x01;
	public static final int BIT_SENSO = 0x02;
	public static final int BIT_SENSU = 0x04;

	public static final int BIT_LED_U = 0x01;
	public static final int BIT_LED_O = 0x02;
	public static final int BIT_LED_FU = 0x04;
	public static final int BIT_LED_FO = 0x08;

	public static final int BIT_UL1 = 0x01;
	public static final int BIT_UL2 = 0x02;
	public static final int BIT_UL3 = 0x04;
	public static final int BIT_IL1 = 0x08;
	public static final int BIT_IL2 = 0x10;
	public static final int BIT_IL3 = 0x20;
	public static final int BIT_IN = 0x40;

	public static final int BIT_RES_LU = 0x01;
	public static final int BIT_SET_LU = 0x02;
	public static final int BIT_RES_LO = 0x04;
	public static final int BIT_SET_LO = 0x08;

	public static final int CMD_STATUS = 1;
	public static final int CMD_UP = 2;
	public static final int CMD_DOWN = 3;
	public static final int CMD_STOP = 4;
	public static final int CMD_TIME = 5;
	public static final int CMD_INP = 6;
	public static final int CMD_OPTO = 7;
	public static final int CMD_SETCNT = 8;
	public static final int CMD_CNT = 9;
	public static final int CMD_RESTIM = 10;
	public static final int CMD_SETAD = 11;
	public static final int CMD_FL_PAGE = 12;
	public static final int CMD_FL_DATA = 13;
	public static final int CMD_FL_PROG = 14;
	public static final int CMD_FL_READ = 15;
	public static final int CMD_RESET = 16;
	public static final int CMD_VERSION = 17;
	public static final int CMD_SERVICECNT = 18;
	public static final int CMD_SET_DOWNCNT = 19;	// Impulse nach Sensor unten
	public static final int CMD_SET_UPCNT = 20;		// Impulse nach Sensor oben
	public static final int CMD_SET_MAXCNT = 21;	// Impulse fuer gesamte Strecke
	public static final int CMD_ERRNR = 22;			// get last Error (and reset errno)
	public static final int CMD_DBG_DATA = 23;		// get some internal data
	public static final int CMD_SET_STATE = 24;		// e.g. set debug state
	public static final int CMD_TEMP = 25;

	//	values for MS State (Statues)
	public static final int MS_RESET = 0;			// ???
	public static final int MS_RDY = 1;
	public static final int MS_UP = 2;
	public static final int MS_DOWN = 3;
	public static final int MS_ERR = 4;
	public static final int MS_DBG = 5;
	public static final int MS_SERVICE = 6;

	//
	public static final int MS_ERR_NOZS = 1;
}
