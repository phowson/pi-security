package net.pisecurity.pi.command;

import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;

import com.google.firebase.database.DatabaseReference;

import net.pisecurity.model.Event;
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.EventType;
import net.pisecurity.model.RequestedState;
import net.pisecurity.pi.main.App;
import net.pisecurity.pi.main.Heartbeater;
import net.pisecurity.pi.monitoring.AlarmBellController;
import net.pisecurity.pi.monitoring.AlertState;
import net.pisecurity.pi.persist.PersistenceService;

public class CommandHandler {
	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CommandHandler.class);

	private AlertState alertState;
	private Executor mainExecutor;
	private net.pisecurity.pi.monitoring.EventListener listener;
	private AlarmBellController alarmBellController;
	private PersistenceService persistenceService;

	private String deviceId;

	private Heartbeater heartbeater;

	public CommandHandler(AlertState alertState, Executor mainExecutor, AlarmBellController alarmBellController,
			net.pisecurity.pi.monitoring.EventListener listener, PersistenceService persistenceService,
			String deviceId, Heartbeater hb) {
		this.alertState = alertState;
		this.mainExecutor = mainExecutor;
		this.listener = listener;
		this.alarmBellController = alarmBellController;
		this.persistenceService = persistenceService;
		this.deviceId = deviceId;
		this.heartbeater = hb;
	}

	public void onCommand(RequestedState request) {
		if (!request.applied) {

			mainExecutor.execute(new Runnable() {

				@Override
				public void run() {
					logger.info("Saw command : " + request);
					switch (request.command) {

					case ARM:
						alertState.armed = true;
						listener.onEvent(new Event(System.currentTimeMillis(), -1, "System manually armed",
								EventType.SYSTEM_MANUAL_ARMED, "Armed manually", deviceId, EventAlertType.NONE, false));

						break;

					case DISARM:
						doResetAlarm();
						alertState.armed = false;
						listener.onEvent(new Event(System.currentTimeMillis(), -1, "System manually disarmed",
								EventType.SYSTEM_MANUAL_DISARMED, "Disarmed manually", deviceId, EventAlertType.NONE,
								false));
						break;

					case RESET_ALARM:
						doResetAlarm();

						listener.onEvent(new Event(System.currentTimeMillis(), -1, "Alarm reset manually",
								EventType.ALARMRESET, "Alarm reset manually", deviceId, EventAlertType.NONE, false));
						break;

					case TRIGGER_ALARM:
						alarmBellController.on();
						alertState.alarmActive = true;

						listener.onEvent(new Event(System.currentTimeMillis(), -1, "Alarm triggered manually",
								EventType.ALARMTRIGGERED_MANUAL, "Alarm triggered manually", deviceId,
								EventAlertType.IMMEDIATE_ALERT, true));
						break;

					}

					request.applied = true;
					persistenceService.persist(request);
					heartbeater.writeHeartbeat();
				}

			});

		}
	}

	private void doResetAlarm() {
		alarmBellController.off();
		alertState.alarmActive = false;
		alertState.firstActivityTs = 0;
	}

}
