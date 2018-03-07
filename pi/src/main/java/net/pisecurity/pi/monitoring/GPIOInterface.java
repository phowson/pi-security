package net.pisecurity.pi.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectProcedure;
import net.pisecurity.pi.util.PinMapping;

public class GPIOInterface implements IOInterface, GpioPinListenerDigital {

	private static final Logger logger = LogManager.getLogger(GPIOInterface.class);

	private final GpioController gpio = GpioFactory.getInstance();

	private TObjectIntHashMap<Pin> reversePinMapping = new TObjectIntHashMap<>();

	private TIntObjectHashMap<List<IOActivityListener>> listeners = new TIntObjectHashMap<>();

	public GPIOInterface() {
		for (int i = 2; i < 22; ++i) {
			reversePinMapping.put(PinMapping.mapPin(i), i);
		}
	}

	@Override
	public synchronized void subscribeEvents(int pin, IOActivityListener listener) {
		// provision gpio pin #02 as an input pin with its internal pull down
		// resistor enabled
		Pin pinCode = PinMapping.mapPin(pin);
		List<IOActivityListener> l = listeners.get(pin);

		if (l == null) {
			l = new ArrayList<>();
			listeners.put(pin, l);
			logger.info("Provisioning pin : " + pinCode);
			final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(pinCode, PinPullResistance.PULL_DOWN);
			gpio.addListener(this, input);
		}
		l.add(listener);

	}

	@Override
	public synchronized void unsubscribeEvents(IOActivityListener listener) {
		listeners.forEachValue(new TObjectProcedure<List<IOActivityListener>>() {

			@Override
			public boolean execute(List<IOActivityListener> arg0) {
				arg0.remove(listener);
				return true;
			}
		});
	}



	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

		if (logger.isInfoEnabled()) {
			logger.info("Saw GPIO Event : " + event);
		}

		int pinNum = reversePinMapping.get(event.getPin());
		List<IOActivityListener> l = listeners.get(pinNum);
		if (l != null) {
			for (IOActivityListener listener : l) {
				try {
					listener.onActivity(pinNum);
				} catch (Exception ex) {
					logger.error("Unexpected exception while dispatching event : " + event, ex);
				}

			}
		}
	}

}
