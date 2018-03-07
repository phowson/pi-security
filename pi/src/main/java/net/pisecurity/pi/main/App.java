package net.pisecurity.pi.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.pisecurity.example.ExampleConfigFactory;
import net.pisecurity.model.AlarmBellConfig;
import net.pisecurity.model.AutoArmConfig;
import net.pisecurity.model.Heartbeat;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.pi.autoarm.AutoArmController;
import net.pisecurity.pi.config.AppConfig;
import net.pisecurity.pi.monitoring.GPIOAlarmBellController;
import net.pisecurity.pi.monitoring.AlertState;
import net.pisecurity.pi.monitoring.EventListener;
import net.pisecurity.pi.monitoring.GPIOInterface;
import net.pisecurity.pi.monitoring.AlarmBellController;
import net.pisecurity.pi.monitoring.IOInterface;
import net.pisecurity.pi.monitoring.InternetStatus;
import net.pisecurity.pi.monitoring.MonitoringService;
import net.pisecurity.pi.persist.FirebasePersistenceService;
import net.pisecurity.pi.persist.PersistenceService;
import net.pisecurity.pi.persist.PersistingEventListener;
import net.pisecurity.util.DoNothingRunnable;
import net.pisecurity.util.NamedThreadFactory;

public class App implements UncaughtExceptionHandler, Runnable {

	private static final Logger logger = LogManager.getLogger(App.class);
	private FirebaseApp firebaseApp;
	private DatabaseReference database;
	private ScheduledExecutorService mainExecutor;
	private AppConfig appConfig;
	private boolean configured;
	private DatabaseReference monitoringConfigRef;
	private MonitoringService monitoringService;
	private AutoArmController autoArmController;
	private AlarmBellController alarmBellController;
	private final AlertState alertState = new AlertState();
	private DatabaseReference locationRef;
	private DatabaseReference eventsRef;
	private EventListener eventListener;
	private PersistenceService persistenceService;
	private IOInterface ioInterface;
	private InternetStatus internetStatus;
	private ScheduledThreadPoolExecutor pingExecutor;
	private DatabaseReference autoArmConfigRef;
	private DatabaseReference alarmBellConfigRef;
	private DatabaseReference hbRef;

	public App(String configFileName, IOInterface ioInterface, AlarmBellController alarmBellController)
			throws FileNotFoundException, IOException {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		this.alarmBellController = alarmBellController;

		logger.info("Initialisng from config file : " + configFileName);

		try (FileReader reader = new FileReader(new File(configFileName))) {
			appConfig = gson.fromJson(reader, AppConfig.class);
		}

		firebaseApp = appConfig.firebaseConfig.createApp();
		logger.info("Firebase app created");
		database = FirebaseDatabase.getInstance(firebaseApp).getReference();

		locationRef = database.child(appConfig.locationId);
		monitoringConfigRef = locationRef.child("monitoringConfig");
		autoArmConfigRef = locationRef.child("autoArmConfig");
		alarmBellConfigRef = locationRef.child("alarmBellConfig");
		eventsRef = locationRef.child("events");
		hbRef = locationRef.child("heartbeat");

		FirebasePersistenceService persistenceService = new FirebasePersistenceService(database, eventsRef, hbRef);
		this.internetStatus = persistenceService;
		this.persistenceService = persistenceService;
		this.eventListener = new PersistingEventListener(persistenceService);
	}

	protected void onMonitoringConfigChange(DataSnapshot snapshot) {

		MonitoringConfig config = snapshot.getValue(MonitoringConfig.class);
		logger.info("Saw config change from firebase : " + config);
		if (config == null) {
			config = ExampleConfigFactory.createMonitoringConfig();
			logger.info("No configuration was seen in firebase, so defaulting to an example config and inserting");
			saveConfig(monitoringConfigRef, config);
		} else {
			configureMonitoring(config);
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

	private void configureMonitoring(MonitoringConfig config) {

		if (monitoringService != null) {
			monitoringService.shutdown();
		}

		monitoringService = new MonitoringService(config, ioInterface, alertState, alarmBellController, eventListener,
				internetStatus, mainExecutor);
		logger.info("Monitoring service configured");

		configured = true;
	}

	private void onMonitoringConfigFetchFailed(DatabaseError error) {
		logger.error("Failed to get monitoring configuration for : " + appConfig.locationId + ", error = " + error);

		if (!configured) {
			logger.error("No configuration for application ever acquired. Shutting down");
			// System.exit(-1);
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String configFileName = args[0];

		new App(configFileName, new GPIOInterface(), new GPIOAlarmBellController()).start();

	}

	private void onAutoArmConfigFetchFailed(DatabaseError error) {
		logger.error("Failed to get auto arm configuration for : " + appConfig.locationId + ", error = " + error);
	}

	private void onAutoArmConfigChange(DataSnapshot snapshot) {
		AutoArmConfig config = snapshot.getValue(AutoArmConfig.class);
		if (config == null) {
			config = ExampleConfigFactory.createAutoArmConfig();
			saveConfig(autoArmConfigRef, config);
		} else {
			this.autoArmController.configure(config);
		}
	}

	private void onAlarmBellConfigChanged(DataSnapshot snapshot) {
		AlarmBellConfig config = snapshot.getValue(AlarmBellConfig.class);
		if (config == null) {
			config = ExampleConfigFactory.createAlarmBellConfig();
			saveConfig(alarmBellConfigRef, config);
		} else {
			this.alarmBellController.configure(config, mainExecutor);
		}
	}

	private void onAlarmBellConfigFetchFailed(DatabaseError error) {
		logger.error("Failed to get alarm bell configuration for : " + appConfig.locationId + ", error = " + error);
	}

	void start() {

		mainExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("MainExecutor", this, false));
		pingExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("PingExecutor", this, false));

		mainExecutor.execute(DoNothingRunnable.INSTANCE);

		this.autoArmController = new AutoArmController(this.internetStatus, mainExecutor, pingExecutor, alertState,
				eventListener);

		monitoringConfigRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				mainExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							onMonitoringConfigChange(snapshot);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}
				});
			}

			@Override
			public void onCancelled(DatabaseError error) {

				mainExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							onMonitoringConfigFetchFailed(error);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}
				});

			}

		});

		autoArmConfigRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				mainExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							onAutoArmConfigChange(snapshot);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}

				});
			}

			@Override
			public void onCancelled(DatabaseError error) {

				mainExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							onAutoArmConfigFetchFailed(error);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}

				});

			}

		});

		alarmBellConfigRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				mainExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							onAlarmBellConfigChanged(snapshot);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}

				});
			}

			@Override
			public void onCancelled(DatabaseError error) {

				mainExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							onAlarmBellConfigFetchFailed(error);
						} catch (Exception ex) {
							logger.error("Unexpected Exception", ex);
						}
					}

				});

			}

		});

		mainExecutor.scheduleWithFixedDelay(this, this.appConfig.heartBeatIntervalMillis,
				this.appConfig.heartBeatIntervalMillis, TimeUnit.MILLISECONDS);

	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.fatal("Uncaught exception on thread : " + t, e);
	}

	@Override
	public void run() {
		try {
			persistenceService.persist(new Heartbeat(System.currentTimeMillis(), alertState.alarmActive,
					alertState.lastAlarmActivation, alertState.armed));
		} catch (Exception e) {
			logger.error("Unexpected exception while persisting heartbeat", e);
		}
	}
}
