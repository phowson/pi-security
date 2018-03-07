package net.pisecurity.pi.persist;

import com.google.firebase.database.DatabaseReference;

import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.pi.monitoring.InternetStatus;

public class FirebasePersistenceService implements PersistenceService, InternetStatus {

	public FirebasePersistenceService(DatabaseReference database, DatabaseReference eventsRef) {
		// TODO Auto-generated constructor stub
	}

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
