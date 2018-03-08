package net.pisecurity.model;

import java.util.List;

public class AlarmBellConfig {
	public List<Integer> outputPins;
	public long maxActivationTimeSeconds;

	@Override
	public String toString() {
		return "AlarmBellConfig [outputPins=" + outputPins + ", maxActivationTimeSeconds=" + maxActivationTimeSeconds
				+ "]";
	}

}
