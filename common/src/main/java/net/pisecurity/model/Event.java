package net.pisecurity.model;

public class Event {
	public long timestamp;
	public int gpioPin;
	public String label;
	public EventType type;
	public String comment;

	public Event(long timestamp, int gpioPin, String label, EventType type, String comment) {
		super();
		this.timestamp = timestamp;
		this.gpioPin = gpioPin;
		this.label = label;
		this.type = type;
		this.comment = comment;
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

		sb.append("\"comment\" : \"");
		sb.append(comment);
		sb.append("\" } ");

		return sb.toString();
	}

}
