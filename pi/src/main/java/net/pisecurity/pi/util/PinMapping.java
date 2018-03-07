package net.pisecurity.pi.util;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class PinMapping {
	public static final Pin mapPin(int pin) {

		String pn = "" + pin;
		if (pn.length() < 2) {
			pn = "0" + pn;
		}
		return RaspiPin.getPinByName("GPIO_" + pn);

	}
}
