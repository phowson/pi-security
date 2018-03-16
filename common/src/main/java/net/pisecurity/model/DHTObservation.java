package net.pisecurity.model;

public class DHTObservation {
	public long time;
	public String location;
	public double temparatureCelcius;
	public double humidityPercent;

	@Override
	public String toString() {
		return "[time=" + time + ", location=" + location + ", temparatureCelcius=" + temparatureCelcius
				+ ", humidityPercent=" + humidityPercent + "]";
	}

}
