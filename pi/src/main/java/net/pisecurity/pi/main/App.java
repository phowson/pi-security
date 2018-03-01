package net.pisecurity.pi.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.pisecurity.pi.config.AppConfig;

public class App {

	private static final Logger logger = LogManager.getLogger(App.class);
	private FirebaseApp firebaseApp;

	public App(String configFileName) throws FileNotFoundException, IOException {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		logger.info("Initialisng from config file : " + configFileName);
		AppConfig ac;
		try (FileReader reader = new FileReader(new File(configFileName))) {
			ac = gson.fromJson(reader, AppConfig.class);
		}

		firebaseApp = ac.firebaseConfig.createApp();
		logger.info("Firebase app created");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String configFileName = args[0];

		new App(configFileName).run();

	}

	private void run() {
		// TODO Auto-generated method stub

	}
}
