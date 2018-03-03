package net.pisecurity.pi.monitoring;

public interface IOInterface {
	public void subscribeEvents(int pin, IOActivityListener listener);

	public void unsubscribeEvents(IOActivityListener listener);
}
