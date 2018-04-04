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

import net.pisecurity.model.DHTObservation;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.EventType;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.RequestedState;
import net.pisecurity.pi.monitoring.EventListener;
import net.pisecurity.pi.monitoring.InternetStatus;
import net.pisecurity.util.NamedThreadFactory;

public class FirebasePersistenceService
		implements PersistenceService, InternetStatus, UncaughtExceptionHandler, Runnable {
	private static final Logger logger = LogManager.getLogger(FirebasePersistenceService.class);
	private static final long RETRY_DELAY_SECONDS = 10;
	private static final long PERSIST_TIMEOUT = 60000;
	private DatabaseReference eventsRef;
	private volatile boolean connected = true;
	private DatabaseReference heartbeatRef;
	private ScheduledThreadPoolExecutor retryScheduler;
	private int maxQueueLength = 10000;
	private DatabaseReference dhtRef;

	private EventListener listener;
	private DatabaseReference requestedStateRef;

	private long lastHeartbeatPersisted;
	private long lastHbTime;
	private String deviceId;

	public FirebasePersistenceService(DatabaseReference database, DatabaseReference eventsRef,
			DatabaseReference heartbeatRef, DatabaseReference requestedStateRef, DatabaseReference dhtRef,
			String deviceId) {
		this.eventsRef = eventsRef;
		this.heartbeatRef = heartbeatRef;
		this.requestedStateRef = requestedStateRef;
		this.dhtRef = dhtRef;
		this.deviceId = deviceId;
		retryScheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Firebase Retry", this, false));
		retryScheduler.scheduleWithFixedDelay(this, PERSIST_TIMEOUT, PERSIST_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	public void setListener(EventListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean isConnected() {
		return connected;

	}

	protected void retry(RequestedState event) {
		if (retryScheduler.getQueue().size() > maxQueueLength) {
			logger.warn("Discarding event due to queue overrun");
			return;
		}
		retryScheduler.schedule(new Runnable() {

			@Override
			public void run() {

				try {
					persist(event);
				} catch (Exception ex) {
					logger.error("Unexpected exception on insert retry for " + event, ex);
				}

			}
		}, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);

	}

	@Override
	public void persist(RequestedState event) {
		requestedStateRef.runTransaction(new Transaction.Handler() {
			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(event);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {

				if (databaseError == null && complete) {
					if (logger.isDebugEnabled()) {
						logger.debug("Command table persisted OK");
					}

					onConnectedChanged(true);
				} else {
					onConnectedChanged(false);
					retry(event);
				}
			}
		});
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
					onConnectedChanged(true);
				} else {
					logger.error("Error reported during save of event. Cannot continue" + databaseError
							+ ", complete = " + complete + ", retries = " + retries);
					onConnectedChanged(false);
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
	public void persist(DHTObservation obs) {
		dhtRef.push().runTransaction(new Transaction.Handler() {
			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(obs);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {
				if (databaseError == null && complete) {
					if (logger.isDebugEnabled()) {
						logger.debug("observation persisted OK");
					}
					onConnectedChanged(true);
				} else {
					logger.fatal("Error reported during save of heartbeat. Cannot continue" + databaseError
							+ ", complete = " + complete);
					onConnectedChanged(false);

				}
			}
		});

	}

	@Override
	public void persist(Heartbeat heartBeat) {
		synchronized (this) {
			lastHbTime = heartBeat.timestamp;
		}

		heartbeatRef.runTransaction(new Transaction.Handler() {

			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(heartBeat);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {

				synchronized (FirebasePersistenceService.this) {
					if (databaseError == null && complete) {
						lastHeartbeatPersisted = System.currentTimeMillis();
						if (logger.isDebugEnabled()) {
							logger.debug("Heartbeat persisted OK");
						}
						if (!connected) {
							logger.info("Internet connection regained");
						}
						onConnectedChanged(true);
					} else {
						logger.fatal("Error reported during save of heartbeat. Cannot continue" + databaseError
								+ ", complete = " + complete);
						onConnectedChanged(false);

					}
				}
			}
		});
	}

	protected synchronized void onConnectedChanged(boolean b) {
		if (!connected) {
			logger.info("Internet connection regained");
		}

		boolean publishEvent = b != connected;
		connected = b;

		if (listener != null && publishEvent) {
			String label;
			EventType et;
			if (b) {
				label = "Internet connection regained";
				et = EventType.INTERNET_ONLINE;
			} else {
				label = "Internet connection lost";
				et = EventType.INTERNET_OFFLINE;
			}

			listener.onEvent(new Event(System.currentTimeMillis(), 1, label, et, label, this.deviceId,
					EventAlertType.NONE, true));
		}

	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.fatal("Saw uncaught exception on thread : " + t, e);
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				if (lastHeartbeatPersisted != 0) {
					long delay = lastHbTime - lastHeartbeatPersisted;
					if (delay > PERSIST_TIMEOUT) {
						onConnectedChanged(false);
					}

				}
			}
		} catch (Exception e) {
			logger.error("Unexpected exception", e);
		}

	}

}
