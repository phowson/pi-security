package net.pisecurity.model;

public class DHTObservation {
	public long time;
	public String location;
	public String deviceId;
	public double temparatureCelcius;
	public double humidityPercent;
	@Override
	public String toString() {
		return "DHTObservation [time=" + time + ", location=" + location + ", deviceId=" + deviceId
				+ ", temparatureCelcius=" + temparatureCelcius + ", humidityPercent=" + humidityPercent + "]";
	}
	

}
