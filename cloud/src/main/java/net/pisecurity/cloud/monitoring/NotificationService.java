package net.pisecurity.cloud.monitoring;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.pisecurity.cloud.model.NotificationConfig;
import net.pisecurity.model.Event;
import net.pisecurity.twillio.TwilioVoiceAlertService;

public class NotificationService {

	private static final Logger logger = LogManager.getLogger(NotificationService.class);

	private TwilioVoiceAlertService voiceAlertService;

	private Executor executor;

	public NotificationService(TwilioVoiceAlertService voiceAlertService, Executor executor) {
		this.voiceAlertService = voiceAlertService;
		this.executor = executor;
	}

	private void sendMessage(String s, NotificationConfig notificationConfig) {

	}

	public void notifyAutoDisarm(NotificationConfig notificationConfig) {
		sendMessage("System automatically disarmed", notificationConfig);

	}

	

	public void reportPinActivity(Event event, NotificationConfig notificationConfig) {
		// TODO Auto-generated method stub

	}

	public void notifyAutoArm(NotificationConfig notificationConfig) {
		// TODO Auto-generated method stub

	}

	public void notifyEvents(NotificationConfig notificationConfig, List<Event> events) {
		// TODO Auto-generated method stub

	}

}
