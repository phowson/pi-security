package net.pisecurity.pi.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import net.pisecurity.pi.monitoring.IOActivityListener;
import net.pisecurity.pi.monitoring.IOInterface;

public class DoNothingIOInterface implements IOInterface, Runnable {
	private static final Logger logger = LogManager.getLogger(DoNothingIOInterface.class);
	private Thread t;
	private TIntObjectHashMap<List<IOActivityListener>> listeners = new TIntObjectHashMap<>();

	public DoNothingIOInterface() {
		t = new Thread(this);
		t.start();
	}

	@Override
	public synchronized void subscribeEvents(int pin, IOActivityListener listener) {
		logger.info("Subcribing to events on pin " + pin + " for listener " + listener);

		List<IOActivityListener> l = listeners.get(pin);
		if (l == null) {
			l = new ArrayList<>();
			listeners.put(pin, l);
			logger.info("Provisioning pin : " + pin);
		}
		l.add(listener);
	}

	@Override
	public synchronized void unsubscribeEvents(IOActivityListener listener) {
		logger.info("Unsubcribing to events for listener " + listener);
		listeners.forEachValue(new TObjectProcedure<List<IOActivityListener>>() {

			@Override
			public boolean execute(List<IOActivityListener> arg0) {
				arg0.remove(listener);
				return true;
			}
		});
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				String s = reader.readLine();
				if (s == null) {
					return;
				}

				int pin = Integer.parseInt(s);
				synchronized (this) {

					List<IOActivityListener> l = listeners.get(pin);
					if (l != null) {
						for (IOActivityListener listener : l) {
							try {
								listener.onActivity(pin);
							} catch (Exception ex) {
								logger.error("Unexpected exception while dispatching event on pin : " + pin, ex);
							}

						}
					}
				}

			} catch (Exception e) {
				logger.error("Error reading from STDIN", e);
			}
		}

	}

}
