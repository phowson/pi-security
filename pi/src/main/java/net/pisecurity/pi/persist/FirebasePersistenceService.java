package net.pisecurity.pi.persist;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import net.pisecurity.model.Event;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.RequestedState;
import net.pisecurity.pi.monitoring.InternetStatus;
import net.pisecurity.util.NamedThreadFactory;

public class FirebasePersistenceService implements PersistenceService, InternetStatus, UncaughtExceptionHandler {
	private static final Logger logger = LogManager.getLogger(FirebasePersistenceService.class);
	private static final long RETRY_DELAY_SECONDS = 10;
	private DatabaseReference eventsRef;
	private volatile boolean connected = true;
	private DatabaseReference heartbeatRef;
	private ScheduledThreadPoolExecutor retryScheduler;
	private int maxQueueLength = 10000;

	public FirebasePersistenceService(DatabaseReference database, DatabaseReference eventsRef,
			DatabaseReference heartbeatRef) {
		this.eventsRef = eventsRef;
		this.heartbeatRef = heartbeatRef;
		retryScheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Firebase Retry", this, false));

	}

	@Override
	public boolean isConnected() {
		return connected;

	}

	@Override
	public void persist(Event event) {
		persistInternal(event, 0);

	}

	private void persistInternal(Event event, int retries) {
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
					logger.error("Error reported during save of event. Cannot continue" + databaseError
							+ ", complete = " + complete + ", retries = " + retries);
					connected = false;

					retry(event, retries + 1);

				}
			}
		});
	}

	protected void retry(Event event, int retries) {
		if (retryScheduler.getQueue().size() > maxQueueLength) {
			logger.warn("Discarding event due to queue overrun");
			return;
		}
		retryScheduler.schedule(new Runnable() {

			@Override
			public void run() {

				try {
					persistInternal(event, retries);
				} catch (Exception ex) {
					logger.error("Unexpected exception on insert retry for " + event, ex);
				}

			}
		}, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);

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

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.fatal("Saw uncaught exception on thread : " + t, e);
	}

	@Override
	public void persist(RequestedState requestedState) {
		// TODO Auto-generated method stub

	}

}
