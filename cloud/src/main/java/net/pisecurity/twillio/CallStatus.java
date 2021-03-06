package net.pisecurity.twillio;

public class CallStatus {

	public long creationTime;
	public String[] numbers;
	public String message;
	public CallStatusListener listener;
	public int index;
	public boolean success;
	public boolean notified;
	public boolean sendSms;

	public CallStatus(long creationTime, String[] numbers, String message, CallStatusListener listener, int index, boolean sendSms) {
		this.creationTime = creationTime;
		this.numbers = numbers;
		this.message = message;
		this.listener = listener;
		this.index = index;
		this.sendSms = sendSms;
	}

}
