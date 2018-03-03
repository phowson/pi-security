package net.pisecurity.pi.monitoring;

import java.util.concurrent.ScheduledExecutorService;

import net.pisecurity.model.MonitoringConfig;

public class MonitoringService {

	private ScheduledExecutorService mainExecutor;
	private IOInterface ioInterface;

	public  MonitoringService(MonitoringConfig config, AlertState alertState,
			ScheduledExecutorService mainExecutor) {
		this.mainExecutor = mainExecutor;
	}

	public void shutdown() {

	}

}
