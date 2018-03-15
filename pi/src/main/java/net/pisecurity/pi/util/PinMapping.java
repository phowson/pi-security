package net.pisecurity.pi.util;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class PinMapping {
	public static final Pin mapPin(int pin) {

		String pn = Integer.toString(pin);
		Pin out = RaspiPin.getPinByName("GPIO " + pn);

		if (out == null) {
			throw new NullPointerException("Problems mapping pin : " + pin);
		}
		return out;

	}
}
