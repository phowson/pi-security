package net.pisecurity.pi.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListener;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.model.PinConfig;
import net.pisecurity.pi.monitoring.GPIOAlarmBellController.PinConf;
import net.pisecurity.pi.util.PinMapping;

public class GPIOAlarmBellController implements Runnable, AlarmBellController {
	public class PinConf {

		private final GpioPinDigitalOutput op;
		private final PinConfig config;

		public PinConf(GpioPinDigitalOutput op, PinConfig p) {
			this.op = op;
			this.config = p;
		}

	}

	private static final Logger logger = LogManager.getLogger(GPIOAlarmBellController.class);
	private final GpioController gpio = GpioFactory.getInstance();
	private ScheduledExecutorService mainExecutor;
	private List<PinConf> outPins = new ArrayList<>();
	private long lastActivationTime;
	private AlarmBellConfig config;

	public GPIOAlarmBellController() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pisecurity.pi.monitoring.IAlarmBellController#configure(net.
	 * pisecurity.model.AlarmBellConfig)
	 */
	@Override
	public synchronized void configure(AlarmBellConfig config, ScheduledExecutorService mainExecutor) {
		this.mainExecutor = mainExecutor;
		logger.info("Reconfiguring alarm, this will turn the alarm off");
		this.config = config;
		for (PinConfig p : config.outputPins) {
			GpioPinDigitalOutput op = gpio.provisionDigitalOutputPin(PinMapping.mapPin(p.pinNumber));
			if (p.activationPinStateHigh) {
				op.setShutdownOptions(true, PinState.LOW);
				op.low();
			} else {
				op.setShutdownOptions(true, PinState.HIGH);
				op.high();
			}
			outPins.add(new PinConf(op, p));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pisecurity.pi.monitoring.IAlarmBellController#on()
	 */
	@Override
	public synchronized void on() {

		logger.info("Turning alarm on");
		lastActivationTime = System.currentTimeMillis();
		for (PinConf p : outPins) {

			if (p.config.activationPinStateHigh) {
				p.op.high();
			} else {
				p.op.low();
			}
		}

		if (mainExecutor != null) {
			mainExecutor.schedule(this, config.maxActivationTimeSeconds, TimeUnit.SECONDS);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pisecurity.pi.monitoring.IAlarmBellController#off()
	 */
	@Override
	public synchronized void off() {
		logger.info("Turning alarm off");
		for (PinConf p : outPins) {
			if (p.config.activationPinStateHigh) {
				p.op.low();
			} else {
				p.op.high();
			}
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
