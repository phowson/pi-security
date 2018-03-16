package net.pisecurity.pi.monitoring;

import com.pi4j.io.gpio.PinPullResistance;

public interface IOInterface {
	public void subscribeEvents(int pin, PinPullResistance res, IOActivityListener listener);

	public void unsubscribeEvents(IOActivityListener listener);
}
