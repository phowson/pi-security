package net.pisecurity.model;

public class RequestedState {

	public long timestamp;
	public Command command;

	public boolean applied;
	public String user;
	@Override
	public String toString() {
		return "RequestedState [timestamp=" + timestamp + ", command=" + command + ", applied=" + applied
				+ ", username=" + user + "]";
	}

	

}
