package net.pisecurity.pi.main;

import java.util.concurrent.ScheduledExecutorService;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.pi.monitoring.AlarmBellController;

public class DoNothingAlarmBellController implements AlarmBellController {

	@Override
	public void on() {
		// TODO Auto-generated method stub

	}

	@Override
	public void off() {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(AlarmBellConfig config, ScheduledExecutorService mainExecutor) {
		// TODO Auto-generated method stub

	}

}
