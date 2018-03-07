package net.pisecurity.pi.main;

import net.pisecurity.pi.monitoring.IOActivityListener;
import net.pisecurity.pi.monitoring.IOInterface;

public class DoNothingIOInterface implements IOInterface {

	@Override
	public void subscribeEvents(int pin, IOActivityListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribeEvents(IOActivityListener listener) {
		// TODO Auto-generated method stub

	}

}
