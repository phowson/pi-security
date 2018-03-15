package net.pisecurity.cloud.model;

import java.util.List;

public class NotificationConfig {
	public boolean sendTextsForAlarm;
	public boolean sendTextsForNotification;
	public boolean sendTextsOnArmDisarm;

	public int heartbeatTimeoutMillis;
	public int callRetries;

	public List<PhoneRecord> alarmNotificationList;
	public List<PhoneRecord> notificationList;

	public String notificationName;

}
