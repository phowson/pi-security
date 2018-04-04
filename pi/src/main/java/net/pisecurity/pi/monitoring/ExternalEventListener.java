package net.pisecurity.pi.monitoring;

import net.pisecurity.model.Event;

public interface ExternalEventListener {
	public void onEvent(Event event);
}
