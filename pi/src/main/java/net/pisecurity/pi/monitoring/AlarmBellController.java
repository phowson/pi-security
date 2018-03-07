package net.pisecurity.pi.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.pi.util.PinMapping;

public class AlarmBellController implements Runnable {
	private static final Logger logger = LogManager.getLogger(AlarmBellController.class);
	private final GpioController gpio = GpioFactory.getInstance();
	private ScheduledExecutorService mainExecutor;
	private List<GpioPinDigitalOutput> outPins = new ArrayList<>();
	private long lastActivationTime;
	private AlarmBellConfig config;

	public AlarmBellController(ScheduledExecutorService mainExecutor) {
		this.mainExecutor = mainExecutor;
	}

	public synchronized void configure(AlarmBellConfig config) {
		logger.info("Reconfiguring alarm, this will turn the alarm off");
		this.config = config;
		for (int p : config.outputPins) {
			GpioPinDigitalOutput op = gpio.provisionDigitalOutputPin(PinMapping.mapPin(p));
			op.setShutdownOptions(true, PinState.LOW);
			op.low();
			outPins.add(op);
		}

	}

	public synchronized void on() {
		logger.info("Turning alarm on");
		lastActivationTime = System.currentTimeMillis();
		for (GpioPinDigitalOutput p : outPins) {
			p.high();
		}

		mainExecutor.schedule(this, config.maxActivationTimeSeconds, TimeUnit.SECONDS);

	}

	public synchronized void off() {
		logger.info("Turning alarm off");
		for (GpioPinDigitalOutput p : outPins) {
			p.low();
		}
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				if (System.currentTimeMillis() - lastActivationTime > config.maxActivationTimeSeconds * 1000) {
					off();
				}
			}
		} catch (Exception ex) {
			logger.error("Unexpected exception checking alarm state", ex);
		}
	}

}
