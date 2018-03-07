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

	@Override
	public String toString() {
		return "AutoArmConfig [autoArmDelaySeconds=" + autoArmDelaySeconds + ", mobileDevicesToTrack="
				+ mobileDevicesToTrack + ", autoArmDisarm=" + autoArmDisarm + ", autoArmTimeZone=" + autoArmTimeZone
				+ ", autoArmStartHour=" + autoArmStartHour + ", autoArmStartMinute=" + autoArmStartMinute
				+ ", autoArmEndHour=" + autoArmEndHour + ", autoArmEndMinute=" + autoArmEndMinute + "]";
	}

}
