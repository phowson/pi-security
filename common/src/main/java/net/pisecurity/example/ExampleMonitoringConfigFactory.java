package net.pisecurity.example;

import java.util.ArrayList;

import net.pisecurity.model.MobileDeviceConfig;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;

public class ExampleMonitoringConfigFactory {

	public static MonitoringConfig create() {
		MonitoringConfig config = new MonitoringConfig();

		config.bellEnabled = true;
		config.autoArmDisarm = true;
		config.alarmDelay = 20000;
		config.autoArmDelay = 60000;

		config.mobileDevicesToTrack = new ArrayList<>();
		MobileDeviceConfig mdc = new MobileDeviceConfig();
		mdc.deviceName = "Example Device";
		mdc.mobileDeviceAddress = "0.0.0.0";

		config.mobileDevicesToTrack.add(mdc);

		config.items = new ArrayList<>();

		for (int i = 2; i < 22; ++i) {
			MonitoredPinConfig e = new MonitoredPinConfig();
			e.enabled = false;
			e.gpioPin = i;
			e.label = "Device on GPIO #" + i;
			e.raiseImmediately = false;
			e.raisesAlert = true;
			e.reportingEnabled = true;
			config.items.add(e);
		}

		return config;
	}

}
