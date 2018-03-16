package net.pisecurity.pi.persist;

import net.pisecurity.model.DHTObservation;
import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.RequestedState;

public interface PersistenceService {

	boolean isConnected();

	public void persist(Event event);

	public void persist(Heartbeat heartBeat);

	public void persist(RequestedState requestedState);

	public void persist(DHTObservation obs);

}