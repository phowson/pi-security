package net.pisecurity.cloud.monitoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseConfig {
	public String databaseURL;
	public String firebaseConfigFile;

	public FirebaseApp createApp() throws FileNotFoundException, IOException {

		try (InputStream serviceAccount = new FileInputStream(firebaseConfigFile)) {
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl(databaseURL).build();

			return FirebaseApp.initializeApp(options);
		}
	}
}
