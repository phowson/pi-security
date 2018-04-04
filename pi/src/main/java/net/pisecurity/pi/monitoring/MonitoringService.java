package net.pisecurity.pi.monitoring;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.pisecurity.model.Edges;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.EventType;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.model.SensorType;

public class MonitoringService implements IOActivityListener, ExternalEventListener {
	private static final Logger logger = LogManager.getLogger(MonitoringService.class);

	private ScheduledExecutorService mainExecutor;
	private IOInterface ioInterface;
	private InternetStatus internetStatus;
	private AlertState alertState;
	private EventListener eventListener;
	private MonitoringConfig config;
	private AlarmBellController alarmBellController;
	private TIntObjectHashMap<MonitoredPinConfig> pinConfigFastLookup = new TIntObjectHashMap<>();

	private String deviceId;

	public MonitoringService(MonitoringConfig config, IOInterface ioInterface, AlertState alertState,
			AlarmBellController alarmBellController, EventListener eventListener, InternetStatus internetStatus,
			ScheduledExecutorService mainExecutor, String deviceId) {
		this.alertState = alertState;
		this.mainExecutor = mainExecutor;
		this.eventListener = eventListener;
		this.ioInterface = ioInterface;
		this.internetStatus = internetStatus;
		this.alarmBellController = alarmBellController;
		this.config = config;
		this.deviceId = deviceId;

		for (MonitoredPinConfig pc : config.items) {
			if (pc.enabled) {
				ioInterface.subscribeEvents(pc.gpioPin, PinPullResistance.valueOf(pc.pullResistance), this);
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
	public void onActivity(int pin, PinEdge pinEdge) {

		long now = System.currentTimeMillis();
		MonitoredPinConfig cfg = pinConfigFastLookup.get(pin);
		if (cfg != null) {

			if (cfg.edges == Edges.BOTH || (cfg.edges == Edges.RISING && pinEdge == PinEdge.RISING)
					|| (cfg.edges == Edges.FALLING && pinEdge == PinEdge.FALLING)) {

				this.eventListener
						.onEvent(new Event(now, pin, cfg.label, EventType.ACTIVITY, "Activity detected on pin " + pin,
								deviceId, getAlertType(cfg), cfg.enabled && (cfg.raisesAlert || cfg.reportingEnabled)));

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
			} else {
				logger.info("Pin " + pin + " activity was not on subscribed edge. Ignoring");
			}

		}

	}

	private EventAlertType getAlertType(MonitoredPinConfig cfg) {

		if (cfg.enabled && (cfg.raisesAlert || cfg.raiseImmediately)) {
			if (cfg.type == SensorType.TAMPER || cfg.raiseImmediately) {
				return EventAlertType.IMMEDIATE_ALERT;
			}
			return EventAlertType.DELAYED_ALERT;
		}

		return EventAlertType.NONE;
	}

	protected void doAlarmCheckAndRaise(long now, MonitoredPinConfig cfg, int pin) {
		if (alertState.armed && !alertState.alarmActive &&
		// Always auto trigger if no internet
				(config.autoTriggerAlarm || !internetStatus.isConnected()) && cfg.raisesAlert) {
			if (alertState.firstActivityTs == 0) {
				alertState.firstActivityTs = now;
			}

			if (cfg.type == SensorType.TAMPER) {
				logger.info("Tamper triggered, immediate raise");
				doRaise("Alarm automatically triggered due to tamper", EventAlertType.IMMEDIATE_ALERT, true);
			} else if (cfg.raiseImmediately) {
				logger.info("Raise immediately flag set on this pin, raise straight away");
				doRaise("Alarm automatically triggered immediately", EventAlertType.IMMEDIATE_ALERT, true);
			} else {

				logger.info("Will re-check for activity in " + config.alarmDelaySeconds + " seconds");
				mainExecutor.schedule(new Runnable() {

					@Override
					public void run() {
						recheckAlarmState();
					}

				}, config.alarmDelaySeconds, TimeUnit.SECONDS);
			}

		}
	}

	protected void recheckAlarmState() {
		long now = System.currentTimeMillis();
		long d = now - alertState.firstActivityTs;
		if (d >= config.alarmDelaySeconds * 1000) {
			logger.info("Alarm should be triggered, has been " + d + "ms since first activity with no response");
			doRaise("Alarm automatically triggered after " + d / 1000 + " seconds", EventAlertType.IMMEDIATE_ALERT,
					true);

		}
	}

	private void doRaise(String str, EventAlertType alert, boolean report) {
		if (alertState.armed && !alertState.alarmActive) {
			if (alertState.firstActivityTs != 0) {
				if (config.bellEnabled) {
					long now = System.currentTimeMillis();

					if (config.autoTriggerAlarm || !internetStatus.isConnected()) {
						alertState.alarmActive = true;
						alertState.lastAlarmActivation = now;
						eventListener.onEvent(new Event(now, -1, "Alarm triggered", EventType.ALARMTRIGGERED_AUTO, str,
								deviceId, alert, report));

						alarmBellController.on();
					}

				} else {
					logger.info("Not triggering at this time");
				}
			}

		}
	}

	@Override
	public void onEvent(Event event) {
		if (!event.deviceId.equals(this.deviceId) && event.alertType != EventAlertType.NONE) {

			logger.info("Saw alertable event " + event + " from another device");
			if (alertState.firstActivityTs == 0) {
				alertState.firstActivityTs = System.currentTimeMillis();
			}

			if (event.alertType == EventAlertType.DELAYED_ALERT) {
				mainExecutor.schedule(new Runnable() {

					@Override
					public void run() {
						recheckAlarmState();
					}

				}, config.alarmDelaySeconds, TimeUnit.SECONDS);
			} else {
				doRaise("Alarm automatically triggered immediately", EventAlertType.IMMEDIATE_ALERT, true);
			}
		}

	}

}
