package net.pisecurity.model;

public class Event {
	public long timestamp;
	public int gpioPin;
	public String label;
	public EventType type;
	public String comment;
	public String deviceId;
	public EventAlertType alertType;
	public boolean notify;

	public Event() {
	}

	public Event(long timestamp, int gpioPin, String label, EventType type, String comment, String deviceId,
			EventAlertType alertType, boolean notify) {
		super();
		this.timestamp = timestamp;
		this.gpioPin = gpioPin;
		this.label = label;
		this.type = type;
		this.comment = comment;
		this.deviceId = deviceId;
		this.alertType = alertType;
		this.notify = notify;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{");

		sb.append("\"timestamp\" : \"");
		sb.append(timestamp);
		sb.append("\", ");

		sb.append("\"gpioPin\" : \"");
		sb.append(gpioPin);
		sb.append("\", ");

		sb.append("\"label\" : \"");
		sb.append(label);
		sb.append("\", ");

		sb.append("\"type\" : \"");
		sb.append(type);
		sb.append("\", ");

		sb.append("\"deviceId\" : \"");
		sb.append(deviceId);
		sb.append("\", ");

		sb.append("\"alertType\" : \"");
		sb.append(alertType);
		sb.append("\", ");

		sb.append("\"notify\" : \"");
		sb.append(notify);
		sb.append("\", ");

		sb.append("\"comment\" : \"");
		sb.append(comment);
		sb.append("\" } ");

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Event [timestamp=" + timestamp + ", gpioPin=" + gpioPin + ", label=" + label + ", type=" + type
				+ ", comment=" + comment + ", deviceId=" + deviceId + ", alertType=" + alertType + ", notify=" + notify
				+ "]";
	}

}
