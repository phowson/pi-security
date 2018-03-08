package net.pisecurity.pi.main;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.pi.monitoring.AlarmBellController;

public class DoNothingAlarmBellController implements AlarmBellController {
	private static final Logger logger = LogManager.getLogger(DoNothingAlarmBellController.class);

	@Override
	public void on() {
		logger.info("Alarm bell turned on");
	}

	@Override
	public void off() {
		logger.info("Alarm bell turned off");

	}

	@Override
	public void configure(AlarmBellConfig config, ScheduledExecutorService mainExecutor) {
		logger.info("Alarm bell reconfigured " + config);
	}

}
