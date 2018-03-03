package net.pisecurity.model;

public class MonitoredPinConfig {
	public int gpioPin;
	public String label;
	public boolean raisesAlert;
	public boolean reportingEnabled;
	public boolean raiseImmediately;
	public boolean enabled;

	@Override
	public String toString() {
		return "MonitoredPinConfig [gpioPin=" + gpioPin + ", label=" + label + ", raisesAlert=" + raisesAlert
				+ ", enabled=" + enabled + "]";
	}

}
