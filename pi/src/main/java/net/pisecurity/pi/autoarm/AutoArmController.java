package net.pisecurity.pi.autoarm;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.pisecurity.model.AutoArmConfig;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventType;
import net.pisecurity.model.MobileDeviceConfig;
import net.pisecurity.pi.monitoring.AlertState;
import net.pisecurity.pi.monitoring.EventListener;
import net.pisecurity.pi.monitoring.InternetStatus;

public class AutoArmController implements Runnable {

	private static final Logger logger = LogManager.getLogger(AutoArmController.class);
	private static final long PING_CHECK_INTERVAL = 1000;
	private static final int PING_TIMEOUT_MILLIS = 250;

	private ScheduledExecutorService mainExecutor;
	private ScheduledExecutorService pingExecutor;
	private AutoArmConfig config;

	private ScheduledFuture<?> future;
	private long devicesLastSeenAt;
	private volatile boolean shutdown;
	private AlertState alertState;
	private EventListener eventListener;
	private ZoneId zone;
	private InternetStatus internetStatus;

	public AutoArmController(InternetStatus internetStatus, ScheduledExecutorService mainExecutor,
			ScheduledExecutorService pingExecutor, AlertState alertState, EventListener eventListener) {
		this.mainExecutor = mainExecutor;
		this.pingExecutor = pingExecutor;
		this.alertState = alertState;
		this.eventListener = eventListener;
		this.internetStatus = internetStatus;
		devicesLastSeenAt = System.currentTimeMillis();
	}

	public void configure(AutoArmConfig config) {
		mainExecutor.execute(new Runnable() {

			@Override
			public void run() {
				if (future != null) {
					logger.info("Shutting down old job");
					future.cancel(true);
				}

				AutoArmController.this.config = config;

				zone = ZoneId.of(config.autoArmTimeZone);

				future = pingExecutor.scheduleWithFixedDelay(AutoArmController.this, PING_CHECK_INTERVAL,
						PING_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
				logger.info("Auto arm controller configured. config = " + config);
			}

		});
	}

	public void shutdown() {
		shutdown = true;
		future.cancel(true);
	}

	@Override
	public void run() {

		if (this.config == null) {
			return;
		}
		try {
			for (MobileDeviceConfig mdc : this.config.mobileDevicesToTrack) {
				InetAddress addr = InetAddress.getByName(mdc.mobileDeviceAddress);

				if (addr.isReachable(PING_TIMEOUT_MILLIS)) {

					if (logger.isDebugEnabled()) {
						logger.debug("Saw device : " + addr);
					}

					long now = System.currentTimeMillis();
					devicesLastSeenAt = now;
					if (config.autoArmDisarm) {
						autoDisarm(mdc.deviceName);
					}
					break;
				}
			}

			long now = System.currentTimeMillis();
			long d = now - devicesLastSeenAt;
			if (d > config.autoArmDelaySeconds * 1000 && config.autoArmDisarm && internetStatus.isConnected()) {
				// Potentially an arm event

				if (isMonitoringPeriod(now)
						&& now - lastMonitoringPeriodStart(now).toInstant().toEpochMilli() > config.autoArmDelaySeconds
								* 1000) {
					autoArm(d);
				}

			}

		} catch (Exception ex) {
			logger.error("Unexpected error checking for devices", ex);
		}
	}

	private void autoDisarm(String deviceName) {
		if (!shutdown) {
			mainExecutor.execute(new Runnable() {
				@Override
				public void run() {

					if (alertState.armed) {
						logger.info("Automatically disarming due to device : " + deviceName);
						alertState.armed = false;
						eventListener.onEvent(new Event(System.currentTimeMillis(), -1, "System automatically disarmed",
								EventType.SYSTEM_AUTO_ARMED, "Disarmed, device " + deviceName + " seen"));
					}

				}
			});
		}

	}

	private ZonedDateTime lastMonitoringPeriodStart(long now) {
		Instant instant = Instant.ofEpochMilli(now);
		ZonedDateTime ldt = ZonedDateTime.ofInstant(instant, zone);
		ZonedDateTime ldtS = ZonedDateTime.ofInstant(instant, zone);
		ldtS = ldtS.withHour(config.autoArmStartHour).withMinute(config.autoArmStartMinute);

		while (ldtS.isAfter(ldt)) {
			ldtS = ldtS.minusDays(1);
		}
		return ldtS;
	}

	private boolean isMonitoringPeriod(long now) {
		Instant instant = Instant.ofEpochMilli(now);
		ZonedDateTime ldt = ZonedDateTime.ofInstant(instant, zone);
		ZonedDateTime ldtS = lastMonitoringPeriodStart(now);

		ZonedDateTime ldtE = ldtS;
		ldtE = ldtE.withHour(config.autoArmEndHour).withMinute(config.autoArmEndMinute);

		if (ldtE.isBefore(ldtS)) {
			ldtE = ldtE.plusDays(1);
		}

		return ldtS.isBefore(ldt) && ldtE.isAfter(ldt);
	}

	private void autoArm(long d) {
		if (!shutdown) {
			mainExecutor.execute(new Runnable() {
				@Override
				public void run() {

					if (!alertState.armed) {
						logger.info("Automatically arming system due to no devices seen for : " + d + " ms");
						alertState.armed = true;

						eventListener.onEvent(new Event(System.currentTimeMillis(), -1, "System automatically armed",
								EventType.SYSTEM_AUTO_ARMED,
								"Armed due to no devices being seen for " + d / 1000 + " seconds"));
					}

				}
			});
		}
	}

}
