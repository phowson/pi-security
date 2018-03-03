package net.pisecurity.pi.monitoring;

import net.pisecurity.model.Event;

public interface EventListener {
	public void onEvent(Event event);
}
