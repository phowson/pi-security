package net.pisecurity.pi.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.pisecurity.example.ExampleMonitoringConfigFactory;
import net.pisecurity.model.MonitoringConfig;
import net.pisecurity.pi.config.AppConfig;
import net.pisecurity.pi.monitoring.AlertState;
import net.pisecurity.pi.monitoring.MonitoringService;
import net.pisecurity.util.DoNothingRunnable;
import net.pisecurity.util.NamedThreadFactory;

public class App implements UncaughtExceptionHandler {

	private static final Logger logger = LogManager.getLogger(App.class);
	private FirebaseApp firebaseApp;
	private DatabaseReference database;
	private ScheduledExecutorService mainExecutor;
	private AppConfig appConfig;
	private boolean configured;
	private DatabaseReference configRef;
	private MonitoringService monitoringService;
	private AlertState alertState = new AlertState();

	public App(String configFileName) throws FileNotFoundException, IOException {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		logger.info("Initialisng from config file : " + configFileName);

		try (FileReader reader = new FileReader(new File(configFileName))) {
			appConfig = gson.fromJson(reader, AppConfig.class);
		}

		firebaseApp = appConfig.firebaseConfig.createApp();
		logger.info("Firebase app created");
		database = FirebaseDatabase.getInstance(firebaseApp).getReference();
		configRef = database.child("monitoringConfig").child(appConfig.locationId);

	}

	protected void onMonitoringConfigChange(DataSnapshot snapshot) {

		MonitoringConfig config = snapshot.getValue(MonitoringConfig.class);
		logger.info("Saw config change from firebase : " + config);
		if (config == null) {
			config = ExampleMonitoringConfigFactory.create();
			logger.info("No configuration was seen in firebase, so defaulting to an example config and inserting");
			saveConfig(config);
		} else {
			configureMonitoring(config);
		}
	}

	private void saveConfig(MonitoringConfig config) {

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

		if (monitoringService == null) {
			monitoringService.shutdown();
		}

		monitoringService = new MonitoringService(config, alertState, mainExecutor);
		logger.info("Monitoring service configured");

		configured = true;
	}

	private void onMonitoringConfigFetchFailed(DatabaseError error) {
		logger.error("Failed to get monitoring configuration for : " + appConfig.locationId + ", error = " + error);

		if (!configured) {
			logger.error("No configuration for application ever acquired. Shutting down");
			System.exit(-1);
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String configFileName = args[0];

		new App(configFileName).run();

	}

	private void run() {

		mainExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("MainExecutor", this, false));
		mainExecutor.execute(DoNothingRunnable.INSTANCE);
		configRef.addValueEventListener(new ValueEventListener() {

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

	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.fatal("Uncaught exception on thread : " + t, e);
	}
}
