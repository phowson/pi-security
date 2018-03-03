package net.pisecurity.pi.persist;

import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;

public interface PersistenceService {

	boolean isConnected();
	
	public void persist(Event event);
	public void persist(Heartbeat heartBeat);

}