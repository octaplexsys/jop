package test;

import joprt.RtThread;

import com.jopdesign.sys.Native;
import util.*;

/**
*	Test.java
*
*	Test board:
*		memory test
*		flash ...
*		io pins on simpexp with Rs
*/

public class TestMem {

	public static void main( String s[] ) {

		Dbg.initSer();
		Timer.wd();

		mem();

		// blink after all tests run
		int cnt = 0;

		for (;;) {

			++cnt;
			if (cnt==100) {
				Timer.wd();
				Dbg.wr('.');
				cnt = 0;
			}
			RtThread.sleepMs(10);
		}
	}
/*
	public static native int rdMem(int adr);
	public static native void wrMem(int val, int adr);
	public static native int rdIntMem(int adr);
*/

	private static final int MEM_SIZE = 262144;		// 1 MB = 256 KW
	/**
	*	perform memory test.
	*/
	private static void mem() {

		int i, j;
		//
		//	internal mem addr 2 is heap pointer
		//
		int start = Native.rdIntMem(2);

		Dbg.intVal(start);
		Dbg.wr('m');
		Dbg.wr('0');
		for (i=start; i<MEM_SIZE; ++i) {
			Native.wrMem(i+1, i);
		}
		Timer.wd();
		Dbg.wr('1');
		for (i=start; i<MEM_SIZE; ++i) {
			j = Native.rdMem(i);
			if (j != i+1) {
				Dbg.wr('f');
				Dbg.intVal(i);
				Dbg.intVal(j);
				Dbg.wr('\n');
				return;
			}
		}
		Timer.wd();
		Dbg.wr('2');
		ok();

	}

	private static void ok() {

		Dbg.wr(' ');
		Dbg.wr('o');
		Dbg.wr('k');
		Dbg.wr('\n');
	}
















}
