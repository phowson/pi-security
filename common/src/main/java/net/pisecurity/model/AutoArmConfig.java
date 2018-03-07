package net.pisecurity.model;

import java.util.List;

public class AutoArmConfig {
	public long autoArmDelaySeconds;
	public List<MobileDeviceConfig> mobileDevicesToTrack;
	public boolean autoArmDisarm;
	public String autoArmTimeZone;
	public int autoArmStartHour;
	public int autoArmStartMinute;

	public int autoArmEndHour;
	public int autoArmEndMinute;

}
