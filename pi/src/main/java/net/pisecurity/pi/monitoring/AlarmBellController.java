package net.pisecurity.pi.monitoring;

import java.util.concurrent.ScheduledExecutorService;

import net.pisecurity.model.AlarmBellConfig;

public interface AlarmBellController {

	void on();

	void off();

	void configure(AlarmBellConfig config, ScheduledExecutorService mainExecutor);

}