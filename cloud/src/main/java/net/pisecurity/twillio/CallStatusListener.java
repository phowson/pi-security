package net.pisecurity.twillio;

public interface CallStatusListener {
	
	public void onCallMade(String number);
	
	public void onCallComplete(boolean success, String answererNumber);
}
