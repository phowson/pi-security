package net.pisecurity.pi.persist;

import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;

public class FirebasePersistenceService implements PersistenceService {

	
	
	
	@Override
	public boolean isConnected() {
		return true;

	}

	@Override
	public void persist(Event event) {
		// TODO Auto-generated method stub

	}

	
	
	@Override
	public void persist(Heartbeat heartBeat) {
		// TODO Auto-generated method stub

	}

}
