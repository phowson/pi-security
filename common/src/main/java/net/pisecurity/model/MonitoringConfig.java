package net.pisecurity.model;

import java.util.List;

public class MonitoringConfig {

	public List<MonitoredPinConfig> items;

	public boolean autoTriggerAlarm;
	public boolean bellEnabled;
	public long alarmDelaySeconds;

	@Override
	public String toString() {
		return "MonitoringConfig [items=" + items + ", autoTriggerAlarm=" + autoTriggerAlarm + ", bellEnabled="
				+ bellEnabled + ", alarmDelay=" + alarmDelaySeconds + "]";
	}

}
