package net.pisecurity.example;

import java.util.ArrayList;
import java.util.Arrays;

import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.model.AutoArmConfig;
import net.pisecurity.model.Command;
import net.pisecurity.model.Edges;
import net.pisecurity.model.MobileDeviceConfig;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.model.PinConfig;
import net.pisecurity.model.RequestedState;
import net.pisecurity.model.SensorType;

public class ExampleConfigFactory {

	public static MonitoringConfig createMonitoringConfig() {
		MonitoringConfig config = new MonitoringConfig();

		config.dhtSensorEnabled = true;
		config.dhtSensorPin = 16;

		config.bellEnabled = true;
		config.alarmDelaySeconds = 20;

		config.items = new ArrayList<>();

		{
			MonitoredPinConfig e = new MonitoredPinConfig();
			e.enabled = true;
			e.gpioPin = 27;
			e.label = "Device on GPIO #" + 27;
			e.type = SensorType.MOTION_SENSOR;
			e.pullResistance = "OFF";
			e.edges = Edges.RISING;
			e.raiseImmediately = false;
			e.raisesAlert = true;
			e.reportingEnabled = true;
			config.items.add(e);
		}

		for (int i : new int[] { 25, 24, 23, 22, 21, 14, 13, 12, 3, 2, 0, 7 }) {
			MonitoredPinConfig e = new MonitoredPinConfig();
			e.enabled = true;
			e.gpioPin = i;
			e.label = "Device on GPIO #" + i;
			e.type = SensorType.MOTION_SENSOR;
			e.pullResistance = "PULL_UP";
			e.edges = Edges.BOTH;
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
		config.outputPins = Arrays.asList(new PinConfig[] { new PinConfig(8, false) });
		config.maxActivationTimeSeconds = 600;

		return config;
	}

	public static RequestedState createRequestedState() {
		RequestedState out = new RequestedState();
		out.applied = true;
		out.command = Command.RESET_ALARM;
		out.timestamp = System.currentTimeMillis();
		return out;
	}

}
