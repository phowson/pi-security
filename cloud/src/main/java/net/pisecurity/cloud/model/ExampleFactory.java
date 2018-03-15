package net.pisecurity.cloud.model;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleFactory {

	private static final Logger logger = LogManager.getLogger(ExampleFactory.class);

	public static NotificationConfig createNotificationConfig() {

		NotificationConfig out = new NotificationConfig();
		out.alarmNotificationList = new ArrayList<>();
		out.alarmNotificationList.add(new PhoneRecord("0", "Example number"));

		out.callRetries = 2;
		out.heartbeatTimeoutMillis = 60000;
		out.notificationList = new ArrayList<>();
		out.notificationList.add(new PhoneRecord("0", "Example number"));

		out.sendTextsForAlarm = true;
		out.sendTextsOnArmDisarm = true;

		return out;
	}
}
