package net.pisecurity.pi.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class GPIOInterface implements IOInterface, GpioPinListenerDigital {
	private final GpioController gpio = GpioFactory.getInstance();

	private Map<Pin, GpioPinDigitalInput> provisionedPins = new HashMap<>();
	private TObjectIntHashMap<Pin> reversePinMapping = new TObjectIntHashMap<>();

	private TIntObjectHashMap<List<IOActivityListener>> listeners = new TIntObjectHashMap<>();

	public GPIOInterface() {
		for (int i = 2; i < 22; ++i) {
			reversePinMapping.put(mapPin(i), i);
		}
	}

	@Override
	public void subscribeEvents(int pin, IOActivityListener listener) {
		// provision gpio pin #02 as an input pin with its internal pull down
		// resistor enabled
		Pin pinCode = mapPin(pin);

		final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(pinCode, PinPullResistance.PULL_DOWN);

		gpio.addListener(this, input);

	}

	@Override
	public void unsubscribeEvents(IOActivityListener listener) {
		// TODO Auto-generated method stub

	}

	private Pin mapPin(int pin) {

		String pn = "" + pin;
		if (pn.length() < 2) {
			pn = "0" + pn;
		}
		return RaspiPin.getPinByName("GPIO_" + pn);

	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
