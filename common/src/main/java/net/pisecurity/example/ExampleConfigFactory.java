package net.pisecurity.example;

import java.util.ArrayList;
import java.util.Arrays;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.model.AutoArmConfig;
import net.pisecurity.model.MobileDeviceConfig;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.model.SensorType;

public class ExampleConfigFactory {

	public static MonitoringConfig createMonitoringConfig() {
		MonitoringConfig config = new MonitoringConfig();

		config.bellEnabled = true;
		config.alarmDelaySeconds = 20;

		config.items = new ArrayList<>();

		for (int i = 5; i < 22; ++i) {
			MonitoredPinConfig e = new MonitoredPinConfig();
			e.enabled = false;
			e.gpioPin = i;
			e.label = "Device on GPIO #" + i;
			e.type = SensorType.MOTION_SENSOR;
			e.raiseImmediately = false;
			e.raisesAlert = true;
			e.reportingEnabled = true;
			config.items.add(e);
		}

		return config;
	}

	public static AutoArmConfig createAutoArmConfig() {
		AutoArmConfig config = new AutoArmConfig();
		config.autoArmDelaySeconds = 600;

		config.autoArmStartHour = 9;
		config.autoArmStartMinute = 0;

		config.autoArmEndHour = 21;
		config.autoArmEndMinute = 0;

		config.autoArmTimeZone = "Europe/London";

		config.mobileDevicesToTrack = new ArrayList<>();
		MobileDeviceConfig mdc = new MobileDeviceConfig();
		mdc.deviceName = "Example Device";
		mdc.mobileDeviceAddress = "0.0.0.0";

		config.mobileDevicesToTrack.add(mdc);

		return config;
	}

	public static AlarmBellConfig createAlarmBellConfig() {
		AlarmBellConfig config = new AlarmBellConfig();
		config.outputPins = Arrays.asList(new Integer[] { new Integer(5) });
		config.maxActivationTimeSeconds = 600;

		return config;
	}

}
