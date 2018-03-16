package net.pisecurity.pi.monitoring;

import com.pi4j.io.gpio.PinEdge;

public interface IOActivityListener {
	public void onActivity(int pin, PinEdge pinEdge);
}
