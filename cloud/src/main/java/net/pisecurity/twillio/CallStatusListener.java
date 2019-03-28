package net.pisecurity.twillio;

public interface CallStatusListener {
	
	public void onCallMade(String number, String message);
	
	public void onCallComplete(boolean success, String answererNumber, String message);
}
