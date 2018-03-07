package net.pisecurity.model;

public class Heartbeat {
	public long timestamp;
	public boolean alarmTriggered;
	public long lastAlarmTime;
	public boolean armed;

	public Heartbeat(long timestamp, boolean alarmTriggered, long lastAlarmTime, boolean armed) {
		super();
		this.timestamp = timestamp;
		this.alarmTriggered = alarmTriggered;
		this.lastAlarmTime = lastAlarmTime;
		this.armed = armed;
	}

}
