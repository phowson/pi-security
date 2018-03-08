package net.pisecurity.model;

public class PinConfig {
	public boolean activationPinStateHigh;
	public int pinNumber;

	public PinConfig() {
	}

	public PinConfig(int num, boolean activationHigh) {
		this.pinNumber = num;
		this.activationPinStateHigh = activationHigh;
	}

	@Override
	public String toString() {
		return "PinConfig [activationPinStateHigh=" + activationPinStateHigh + ", pinNumber=" + pinNumber + "]";
	}

}
