package net.pisecurity.pi.main;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TestApp {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String configFileName = args[0];

		new App(configFileName, new DoNothingIOInterface()).start();
	}

}
