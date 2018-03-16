package net.pisecurity.model;

public class MonitoredPinConfig {
	public int gpioPin;
	public String label;
	public SensorType type;
	public boolean raisesAlert;
	public boolean reportingEnabled;
	public boolean raiseImmediately;
	public boolean enabled;
	public String pullResistance;
	public Edges edges;

	@Override
	public String toString() {
		return "MonitoredPinConfig [gpioPin=" + gpioPin + ", label=" + label + ", type=" + type + ", raisesAlert="
				+ raisesAlert + ", reportingEnabled=" + reportingEnabled + ", raiseImmediately=" + raiseImmediately
				+ ", enabled=" + enabled + ", pullResistance=" + pullResistance + ", edges=" + edges + "]";
	}

}
