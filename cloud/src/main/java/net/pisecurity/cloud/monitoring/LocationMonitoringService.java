package net.pisecurity.cloud.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import net.pisecurity.cloud.model.CommandRecord;
import net.pisecurity.cloud.model.ExampleFactory;
import net.pisecurity.cloud.model.NotificationConfig;
import net.pisecurity.model.Event;
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.EventPersistenceService;
import net.pisecurity.model.EventType;
import net.pisecurity.model.Heartbeat;

public class LocationMonitoringService implements Runnable {

	private static final Logger logger = LogManager.getLogger(LocationMonitoringService.class);

	private static final long HEARTBEAT_INTERVAL = 10 * 1000;

	private ScheduledExecutorService mainExecutor;
	private DatabaseReference heartbeatRef;
	private DatabaseReference monitoringConfigRef;

	private DatabaseReference configRef;
	private Map<String, Long> lastHeartbeatTime = new HashMap<>();
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

	private long startupTime;

	private Set<String> heartbeatAlerted = new HashSet<>();

	private EventPersistenceService eventPersistenceService;

	private DatabaseReference cloudCommandRef;

	private DatabaseReference cloudHeartbeatRef;

	public LocationMonitoringService(String locationId, ScheduledExecutorService mainExecutor, AppConfig appConfig,
			DatabaseReference locationRef,

			EventPersistenceService eventPersistenceService, NotificationService notificationService) {
		this.location = locationId;
		this.eventPersistenceService = eventPersistenceService;
		this.mainExecutor = mainExecutor;
		this.notificationService = notificationService;
		monitoringConfigRef = locationRef.child("cloudMonitoringConfig");
		heartbeatRef = locationRef.child("heartbeat");
		configRef = locationRef.child("notificationConfig");

		cloudHeartbeatRef = locationRef.child("lastCloudHeartbeat");
		eventsRef = locationRef.child("events");

		startupTime = System.currentTimeMillis();

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
		cloudCommandRef = locationRef.child("cloudCommand");

		cloudCommandRef.addValueEventListener(new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				CommandRecord r = snapshot.getValue(CommandRecord.class);
				if (r!=null) {
					if (!r.applied) {
						onNewCloudServiceCommand(r);
					}
				}

			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				
			}
		});

		mainExecutor.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {

				try {
					cloudHeartbeatRef.setValue(System.currentTimeMillis(), new CompletionListener() {

						@Override
						public void onComplete(DatabaseError error, DatabaseReference ref) {
							if (error != null) {
								logger.error("Error while writing heartbeat : " + error);
							}
						}
					});
				} catch (Exception e) {
					logger.error("Error while writing heartbeat : ", e);
				}

			}
		}, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

	}

	protected void onNewCloudServiceCommand(CommandRecord r) {
		logger.info("Recieved new cloud command : " + r.command);

		switch (r.command) {

		case "TEST_CALL":
			notificationService
					.notifyEvents(location, notificationConfig,
							java.util.Collections.singletonList(new Event(System.currentTimeMillis(), -1,
									"Test cloud call service", EventType.ACTIVITY, "Test cloud call service", "cloud",
									EventAlertType.IMMEDIATE_ALERT, true)));

			break;

		default:
			logger.error("Unknown command : " + r.command);
		}

		CommandRecord r2 = new CommandRecord();
		r2.command = r.command;
		r2.applied = true;
		cloudCommandRef.setValue(r2, new CompletionListener() {

			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					logger.error("Error while updating command : " + error);

				}
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

			eventsRef.orderByChild("timestamp").startAt(startupTime).addChildEventListener(new ChildEventListener() {

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

		if (event.timestamp > startupTime) {

			logger.info("Saw event : " + event);
			switch (event.type) {
			case ALARMTRIGGERED_MANUAL:
			case ALARMTRIGGERED_AUTO:
				events.add(event);
				startBatch();

				break;

			case ALARMRESET:
				notifyReset();
				break;

			case SYSTEM_MANUAL_ARMED:
			case SYSTEM_AUTO_ARMED:
				notifyAutoArm();
				break;

			case SYSTEM_MANUAL_DISARMED:
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
					} else if (event.notify) {
						// Not alertable, but still notifyable.
						reportPinActivity(event);
					}

				}

			default:
			}

		} else {
			logger.info("Discarded stale event :" + event);
		}

	}

	private void startBatch() {
		if (!batchingEvents) {
			logger.info("Starting batching of events");
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

	private void notifyReset() {

		notificationService.notifyReset(notificationConfig);

	}

	private void notifyAutoArm() {
		notificationService.notifyAutoArm(notificationConfig);
	}

	protected synchronized void sendBatch(boolean force) {

		if (force || (System.currentTimeMillis() - batchStart >= monitoringConfig.alarmDelaySeconds * 1000 && armed)) {
			logger.info("Sending alert batch : " + events);

			this.eventPersistenceService.persist(
					new Event(System.currentTimeMillis(), -1, "Users notified", EventType.USERS_NOTIFIED_OF_ALARM,
							"Users notified by cloud service", "Cloud Service", EventAlertType.NONE, false));

			notificationService.notifyEvents(this.location, notificationConfig, events);
			batchingEvents = false;
			events.clear();
		}

	}

	protected synchronized void onHeartbeatChanged(DataSnapshot snapshot) {
		boolean anyArmed = false;
		for (Iterator<DataSnapshot> it = snapshot.getChildren().iterator(); it.hasNext();) {
			DataSnapshot n = it.next();
			String deviceName = n.getKey();
			Heartbeat hb = n.getValue(Heartbeat.class);
			if (hb != null) {
				Long lhbt = lastHeartbeatTime.get(deviceName);

				if (lhbt == null || hb.timestamp > lhbt) {
					logger.info("device heartbeat : " + hb + " for location : " + location);
					lastHeartbeatTime.put(deviceName, hb.timestamp);
				}
				anyArmed |= hb.armed;
			} else {
				logger.error("Location " + location + " has no heartbeat");
			}
		}
		if (this.armed != anyArmed) {
			this.armed = anyArmed;
			logger.info("System armed status is now : " + armed);
			events.clear();
			batchingEvents = false;
			batchStart = 0;
		}
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

		try {
			if (this.monitoringConfig != null) {

				for (Map.Entry<String, Long> e : lastHeartbeatTime.entrySet()) {
					long d = System.currentTimeMillis() - e.getValue();

					String deviceName = e.getKey();
					if (d / 1000 > this.monitoringConfig.alarmDelaySeconds) {

						if (!heartbeatAlerted.contains(deviceName)) {
							logger.info("Saw heartbeat timeout, " + (d / 1000)
									+ " seconds without any heartbeat activity for device " + deviceName);

							notificationService.notifyHeartbeatTimeout(this.location, deviceName, notificationConfig);

							heartbeatAlerted.add(deviceName);
						}

					} else {
						if (heartbeatAlerted.contains(deviceName)) {
							logger.info("Heartbeat now OK for " + deviceName);
							heartbeatAlerted.remove(deviceName);
						}
					}
				}

			}
		} catch (Exception e) {
			logger.error("Unexpected exception while checking heartbeat", e);
		}

	}

}
