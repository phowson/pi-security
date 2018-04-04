package net.pisecurity.cloud.monitoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.pisecurity.cloud.model.ExampleFactory;
import net.pisecurity.cloud.model.NotificationConfig;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.MonitoredPinConfig;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.twillio.CallStatusListener;

public class LocationMonitoringService implements Runnable, CallStatusListener {

	private static final Logger logger = LogManager.getLogger(LocationMonitoringService.class);

	private ScheduledExecutorService mainExecutor;
	private DatabaseReference heartbeatRef;
	private DatabaseReference monitoringConfigRef;
	private DatabaseReference callsRef;
	private DatabaseReference configRef;
	private long lastHeartbeatTime;
	private CloudMonitoringConfig monitoringConfig;

	private String location;

	private ScheduledFuture<?> heartbeatCheckFuture;

	private NotificationConfig notificationConfig;

	private DatabaseReference eventsRef;

	private boolean eventsSubscribed;

	private List<Event> events = new ArrayList<>();

	private boolean batchingEvents;

	private long batchStart;

	private boolean armed;

	private NotificationService notificationService;

	public LocationMonitoringService(String locationId, ScheduledExecutorService mainExecutor, AppConfig appConfig,
			DatabaseReference locationRef, NotificationService notificationService) {
		this.location = locationId;
		this.mainExecutor = mainExecutor;
		this.notificationService = notificationService;
		monitoringConfigRef = locationRef.child("cloudMonitoringConfig");
		heartbeatRef = locationRef.child("heartbeat");
		callsRef = locationRef.child("calls");
		configRef = locationRef.child("notificationConfig");

		eventsRef = locationRef.child("events");

		lastHeartbeatTime = System.currentTimeMillis();

		configRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				onNotificationConfigChanged(snapshot);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				logger.error("Database error while trying to get monitoring config " + error);
			}
		});

		monitoringConfigRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				onMonitoringConfigChanged(snapshot);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				logger.error("Database error while trying to get monitoring config " + error);
			}
		});

		heartbeatRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				onHeartbeatChanged(snapshot);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				logger.error("Database error while trying to get heartbeat " + error);
			}
		});

	}

	protected synchronized void onNotificationConfigChanged(DataSnapshot snapshot) {
		NotificationConfig config = snapshot.getValue(NotificationConfig.class);
		if (config == null) {
			config = ExampleFactory.createNotificationConfig();
			saveConfig(configRef, config);
		} else {

			logger.info("Saw new notification config " + config);
			this.notificationConfig = config;

			if (heartbeatCheckFuture != null) {
				heartbeatCheckFuture.cancel(true);
			}

			long d = Math.max(1000, config.heartbeatTimeoutMillis / 4);
			heartbeatCheckFuture = mainExecutor.scheduleWithFixedDelay(this, d, d, TimeUnit.MILLISECONDS);

			subscribeEvents();

		}

	}

	private void subscribeEvents() {
		if (!eventsSubscribed && this.monitoringConfig != null && this.notificationConfig != null) {

			eventsRef.addChildEventListener(new ChildEventListener() {

				@Override
				public void onChildRemoved(DataSnapshot snapshot) {

				}

				@Override
				public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

				}

				@Override
				public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

				}

				@Override
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
					Event event = snapshot.getValue(Event.class);
					onNewEvent(event);

				}

				@Override
				public void onCancelled(DatabaseError error) {

				}
			});
		}
	}

	protected void onNewEvent(Event event) {

		switch (event.type) {
		case ALARMTRIGGERED_AUTO:
			events.add(event);
			startBatch();

			break;

		case SYSTEM_AUTO_ARMED:
			notifyAutoArm();
			break;

		case SYSTEM_AUTO_DISARMED:
			notifyAutoDisarm();
			break;

		case ACTIVITY:
			if (armed) {

				if (event.alertType != EventAlertType.NONE) {
					events.add(event);
					startBatch();
					if (event.alertType == EventAlertType.IMMEDIATE_ALERT) {
						sendBatch(true);
					}
				}

				if (event.notify) {
					reportPinActivity(event);
				}

			}

		default:
		}

	}

	private void startBatch() {
		if (!batchingEvents) {
			batchingEvents = true;
			batchStart = System.currentTimeMillis();
			mainExecutor.schedule(new Runnable() {

				@Override
				public void run() {
					sendBatch(false);

				}
			}, monitoringConfig.alarmDelaySeconds, TimeUnit.SECONDS);
		}
	}

	private void notifyAutoDisarm() {
		notificationService.notifyAutoDisarm(notificationConfig);
	}

	private void reportPinActivity(Event event) {
		notificationService.reportPinActivity(event, notificationConfig);

	}

	private void notifyAutoArm() {
		notificationService.notifyAutoArm(notificationConfig);
	}

	protected synchronized void sendBatch(boolean force) {

		if (force || System.currentTimeMillis() - batchStart >= monitoringConfig.alarmDelaySeconds * 1000) {
			logger.info("Sending alert batch : " + events);
			notificationService.notifyEvents(notificationConfig, events);
			events.clear();
		}
	}

	protected synchronized void onHeartbeatChanged(DataSnapshot snapshot) {
		boolean anyArmed = false;
		for (Iterator<DataSnapshot> it = snapshot.getChildren().iterator(); it.hasNext();) {
			Heartbeat hb = it.next().getValue(Heartbeat.class);
			if (hb != null) {
				if (hb.timestamp > lastHeartbeatTime) {
					logger.info("device heartbeat : " + hb + " for location : " + location);
					lastHeartbeatTime = hb.timestamp;
				}
				anyArmed |= hb.armed;
			} else {
				logger.error("Location " + location + " has no heartbeat");
			}
		}
		this.armed = anyArmed;
	}

	private void saveConfig(DatabaseReference configRef, Object config) {

		configRef.runTransaction(new Transaction.Handler() {
			public Transaction.Result doTransaction(MutableData mutableData) {
				mutableData.setValue(config);
				return Transaction.success(mutableData);
			}

			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot dataSnapshot) {
				if (databaseError == null && complete) {
					logger.info("Configuration saved : " + config);
				} else {
					logger.fatal("Error reported during configuration save. Cannot continue" + databaseError
							+ ", complete = " + complete);
					System.exit(-2);
				}
			}
		});
	}

	protected synchronized void onMonitoringConfigChanged(DataSnapshot snapshot) {
		CloudMonitoringConfig config = snapshot.getValue(CloudMonitoringConfig.class);

		logger.info("Saw new monitoring config " + config);

		if (config == null) {
			// Create null monitoring config
			CloudMonitoringConfig cfg = new CloudMonitoringConfig();
			cfg.alarmDelaySeconds = 30;
			saveConfig(this.monitoringConfigRef, cfg);
		} else {
			this.monitoringConfig = config;

			subscribeEvents();
		}
	}

	public synchronized void dispose() {
		if (heartbeatCheckFuture != null) {
			heartbeatCheckFuture.cancel(true);
		}
	}

	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void onCallMade(String number) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void onCallComplete(boolean success, String answererNumber) {
		// TODO Auto-generated method stub

	}

}
