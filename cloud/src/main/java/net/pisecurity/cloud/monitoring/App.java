package net.pisecurity.cloud.monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.pisecurity.twillio.TwilioAccountDetails;
import net.pisecurity.twillio.TwilioVoiceAlertService;
import net.pisecurity.util.DoNothingRunnable;
import net.pisecurity.util.NamedThreadFactory;

public class App implements UncaughtExceptionHandler, java.util.concurrent.RejectedExecutionHandler {
	private static final Logger logger = LogManager.getLogger(App.class);

	private FirebaseApp firebaseApp;
	private DatabaseReference database;

	private Map<String, LocationMonitoringService> locations = new HashMap<>();

	private ScheduledThreadPoolExecutor mainExecutor;

	private AppConfig appConfig;

	protected NotificationService notificationService;

	private TwilioVoiceAlertService voiceAlertService;

	public App(String configFileName) throws Exception {
		logger.info("Initialisng from config file : " + configFileName);

		mainExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("MainExecutor", this, false), this);
		// Forces start of thread pool.
		mainExecutor.execute(new DoNothingRunnable());
		GsonBuilder builder = new GsonBuilder();

		Gson gson = builder.create();

		try (FileReader reader = new FileReader(new File(configFileName))) {
			appConfig = gson.fromJson(reader, AppConfig.class);
		}

		TwilioAccountDetails twilioAccountDetails;
		logger.info("Loading Twilio config from : " + appConfig.twilioConfigFile);
		try (FileReader reader = new FileReader(new File(appConfig.twilioConfigFile))) {
			twilioAccountDetails = gson.fromJson(reader, TwilioAccountDetails.class);
		}

		firebaseApp = appConfig.firebaseConfig.createApp();
		logger.info("Firebase app created");
		database = FirebaseDatabase.getInstance(firebaseApp).getReference();

		voiceAlertService = new TwilioVoiceAlertService(twilioAccountDetails, appConfig.serverConfig);
		logger.info("Starting Twilio alerting service...");
		voiceAlertService.start();

		Executor executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("TwillioThread", this, false));
		notificationService = new NotificationService(voiceAlertService, executor);

		database.addChildEventListener(new ChildEventListener() {

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				LocationMonitoringService serv = locations.remove(snapshot.getKey());
				if (serv != null) {
					serv.dispose();
				}
			}

			@Override
			public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
				LocationMonitoringService serv = locations.remove(snapshot.getKey());
				if (serv != null) {
					serv.dispose();
				}
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			}

			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

				DataSnapshot s = snapshot;
				LocationMonitoringService service = locations.get(s.getKey());
				if (service == null) {
					logger.info("Configuring for location : " + s.getKey());
					service = new LocationMonitoringService(s.getKey(), mainExecutor, appConfig, s.getRef(),
							notificationService);
					locations.put(s.getKey(), service);
				}
			}

			@Override
			public void onCancelled(DatabaseError error) {
				logger.error("Firebase error: " + error);

			}
		});
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		logger.error("Execution rejected " + r);

	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught exception on thread" + t, e);
	}

	public static void main(String[] args) throws Exception {
		new App(args[0]);
	}
}
