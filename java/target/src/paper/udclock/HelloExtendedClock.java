/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package udclock;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.*;
import javax.safetycritical.io.SimplePrintStream;

/**
 * A minimal SCJ application to show a passive clock
 * with the extended model of user defined clocks.
 * 
 * @author Martin Schoeberl
 * 
 */
public class HelloExtendedClock extends Mission implements Safelet {

	// work around...
	static HelloExtendedClock single;
	
	static PassiveExtendedClock counter;

	static SimplePrintStream out;

	// From Mission
	@Override
	protected void initialize() {

		OutputStream os = null;
		try {
			os = Connector.openOutputStream("console:");
		} catch (IOException e) {
			throw new Error("No console available");
		}
		out = new SimplePrintStream(os);
		
		counter = new PassiveExtendedClock();

		PeriodicEventHandler peh = new PeriodicEventHandler(
				new PriorityParameters(11), new PeriodicParameters(
						new RelativeTime(0, 0), new RelativeTime(1000, 0)),
				new StorageParameters(10000, 1000, 1000)) {
			int cnt;
			UserTick dest = new UserTick(0, counter);

			public void handleAsyncEvent() {
				// The following type conversion is needed as we have
				// changed the classes to the new model.
				// Not needed in the RTSJ 1.1 version
				AbsoluteTime time = (AbsoluteTime) Clock.getRealtimeClock().getTime();
				out.print("It is " + time.getMilliseconds());
				counter.getTime(dest);
				out.println(" counter " + dest.getTicks());
				++cnt;
				if (cnt > 5) {
					// getCurrentMission is not yet working
					single.requestTermination();
				}
			}
		};
		peh.register();
	}

	// Safelet methods
	@Override
	public MissionSequencer getSequencer() {
		// we assume this method is invoked only once
		StorageParameters sp = new StorageParameters(1000000, 0, 0);
		return new LinearMissionSequencer(new PriorityParameters(13), sp, this);
	}

	@Override
	public long missionMemorySize() {
		return 100000;
	}

	/**
	 * Within the JOP SCJ version we use a main method instead of a command line
	 * parameter or configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Terminal.getTerminal().writeln("Hello SCJ World!");
		single = new HelloExtendedClock();
		JopSystem.startMission(single);
	}

}
