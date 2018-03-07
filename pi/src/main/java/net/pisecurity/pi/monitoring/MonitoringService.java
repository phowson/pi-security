package net.pisecurity.pi.monitoring;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventType;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;

public class MonitoringService implements IOActivityListener {
	private static final Logger logger = LogManager.getLogger(MonitoringService.class);

	private ScheduledExecutorService mainExecutor;
	private IOInterface ioInterface;
	private InternetStatus internetStatus;
	private AlertState alertState;
	private EventListener eventListener;
	private MonitoringConfig config;
	private AlarmBellController alarmBellController;
	private TIntObjectHashMap<MonitoredPinConfig> pinConfigFastLookup = new TIntObjectHashMap<>();

	public MonitoringService(MonitoringConfig config, IOInterface ioInterface, AlertState alertState,
			AlarmBellController alarmBellController, EventListener eventListener, InternetStatus internetStatus,
			ScheduledExecutorService mainExecutor) {
		this.alertState = alertState;
		this.mainExecutor = mainExecutor;
		this.eventListener = eventListener;
		this.ioInterface = ioInterface;
		this.alarmBellController = alarmBellController;
		this.config = config;

		for (MonitoredPinConfig pc : config.items) {
			if (pc.enabled) {
				ioInterface.subscribeEvents(pc.gpioPin, this);
				pinConfigFastLookup.put(pc.gpioPin, pc);
			}

		}

	}

	public void shutdown() {
		for (MonitoredPinConfig pc : config.items) {
			if (pc.enabled) {
				ioInterface.unsubscribeEvents(this);
			}

		}

	}

	@Override
	public void onActivity(int pin) {

		long now = System.currentTimeMillis();
		MonitoredPinConfig cfg = pinConfigFastLookup.get(pin);
		if (cfg != null) {
			this.eventListener
					.onEvent(new Event(now, pin, cfg.label, EventType.ACTIVITY, "Activity detected on pin " + pin));

			mainExecutor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						doAlarmCheckAndRaise(now, cfg, pin);
					} catch (Exception ex) {
						logger.error("Unexpected exception while checking alarm state for " + pin, ex);
					}
				}
			});

		}

	}

	protected void doAlarmCheckAndRaise(long now, MonitoredPinConfig cfg, int pin) {
		if (alertState.armed && !alertState.alarmActive && config.bellEnabled) {
			if (alertState.firstActivityTs == 0) {
				alertState.firstActivityTs = now;
			}

			mainExecutor.schedule(new Runnable() {

				@Override
				public void run() {
					recheckAlarmState();
				}

			}, config.alarmDelaySeconds, TimeUnit.SECONDS);

		}
	}

	protected void recheckAlarmState() {

		if (alertState.armed && !alertState.alarmActive) {
			if (alertState.firstActivityTs != 0) {
				if (config.bellEnabled) {

					long now = System.currentTimeMillis();
					long d = now - alertState.firstActivityTs;
					if (d >= config.alarmDelaySeconds * 1000) {
						logger.info("Alarm should be triggered, has been " + d
								+ "ms since first activity with no response");
						if (config.autoTriggerAlarm || !internetStatus.isConnected()) {
							alertState.alarmActive = true;
							alertState.lastAlarmActivation = now;
							eventListener.onEvent(new Event(now, -1, "Alarm triggered", EventType.ALARMTRIGGERED_AUTO,
									"Alarm automatically triggered after " + d / 1000 + " seconds"));

							alarmBellController.on();

						}

					} else {
						logger.info("Not triggering at this time");
					}
				}

			}
		}
	}

}
