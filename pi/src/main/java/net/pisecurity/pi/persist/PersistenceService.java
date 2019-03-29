package net.pisecurity.pi.persist;

import net.pisecurity.model.DHTObservation;
import net.pisecurity.model.EventPersistenceService;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.RequestedState;

public interface PersistenceService extends EventPersistenceService{

	boolean isConnected();


	public void persist(Heartbeat heartBeat);

	public void persist(RequestedState requestedState);

	public void persist(DHTObservation obs);

}