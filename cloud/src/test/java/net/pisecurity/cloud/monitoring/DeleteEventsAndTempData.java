package net.pisecurity.cloud.monitoring;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.pisecurity.twillio.TwilioAccountDetails;

public class DeleteEventsAndTempData {

	private static final Logger logger = LogManager.getLogger(DeleteEventsAndTempData.class);

	public static void main(String[] args) throws Exception {
		String configFileName = args[0];
		
		GsonBuilder builder = new GsonBuilder();

		Gson gson = builder.create();
		AppConfig appConfig;
		try (FileReader reader = new FileReader(new File(configFileName))) {
			appConfig = gson.fromJson(reader, AppConfig.class);
		}
		FirebaseApp firebaseApp = appConfig.firebaseConfig.createApp();
		logger.info("Firebase app created");
		DatabaseReference database = FirebaseDatabase.getInstance(firebaseApp).getReference();
		DatabaseReference c = database.child("locations/test-1/events");
		c.removeValueAsync().get();
		
		c = database.child("locations/test-1/humidityTemperature");
		c.removeValueAsync().get();
		
		c = database.child("calls");
		c.removeValueAsync().get();
	}
}
