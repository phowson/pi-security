package net.pisecurity.pi.persist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.pi.monitoring.InternetStatus;

public class FirebasePersistenceService implements PersistenceService, InternetStatus {
	private static final Logger logger = LogManager.getLogger(FirebasePersistenceService.class);
	private DatabaseReference eventsRef;
	private volatile boolean connected = true;
	private DatabaseReference heartbeatRef;

	public FirebasePersistenceService(DatabaseReference database, DatabaseReference eventsRef,
			DatabaseReference heartbeatRef) {
		this.eventsRef = eventsRef;
		this.heartbeatRef = heartbeatRef;
	}

	@Override
	public boolean isConnected() {
		return connected;

	}

	@Override
	public void persist(Event event) {
		eventsRef.push().runTransaction(new Transaction.Handler() {
			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(event);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {
				if (databaseError == null && complete) {
					if (logger.isDebugEnabled()) {
						logger.debug("Heartbeat persisted OK");
					}
					if (!connected) {
						logger.info("Internet connection regained");
					}
					connected = true;
				} else {
					logger.fatal("Error reported during save of heartbeat. Cannot continue" + databaseError
							+ ", complete = " + complete);
					connected = false;

				}
			}
		});

	}

	@Override
	public void persist(Heartbeat heartBeat) {
		heartbeatRef.runTransaction(new Transaction.Handler() {
			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(heartBeat);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {
				if (databaseError == null && complete) {
					if (logger.isDebugEnabled()) {
						logger.debug("Heartbeat persisted OK");
					}
					if (!connected) {
						logger.info("Internet connection regained");
					}
					connected = true;
				} else {
					logger.fatal("Error reported during save of heartbeat. Cannot continue" + databaseError
							+ ", complete = " + complete);
					connected = false;

				}
			}
		});
	}

}
