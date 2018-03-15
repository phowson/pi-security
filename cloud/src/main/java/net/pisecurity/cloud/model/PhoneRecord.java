package net.pisecurity.cloud.model;

public class PhoneRecord {
	public String number;
	public String label;
	
	
	public PhoneRecord() {
	}


	public PhoneRecord(String number, String label) {
		this.number = number;
		this.label = label;
	}


	@Override
	public String toString() {
		return "PhoneRecord [number=" + number + ", label=" + label + "]";
	}
	
	

}
