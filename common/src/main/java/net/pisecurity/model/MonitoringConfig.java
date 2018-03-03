package net.pisecurity.model;

import java.util.List;

public class MonitoringConfig {

	public List<MonitoredPinConfig> items;
	public List<MobileDeviceConfig> mobileDevicesToTrack;
	
	public boolean autoArmDisarm;
	public boolean autoTriggerAlarm;
	public boolean bellEnabled;	
	public long alarmDelay;
	public long autoArmDelay;
	
	

	@Override
	public String toString() {
		return "MonitoringConfig [items=" + items + ", autoArmDisarm=" + autoArmDisarm + ", autoTriggerAlarm="
				+ autoTriggerAlarm + ", alarmDelay=" + alarmDelay + "]";
	}
	
	
	
	
}
