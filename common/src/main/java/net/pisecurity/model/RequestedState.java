package net.pisecurity.model;

public class RequestedState {

	public long timestamp;
	public Command command;

	public boolean applied;

	@Override
	public String toString() {
		return "[timestamp=" + timestamp + ", command=" + command + ", applied=" + applied + "]";
	}

}
