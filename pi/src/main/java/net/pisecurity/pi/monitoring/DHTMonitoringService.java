package net.pisecurity.pi.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.pisecurity.model.DHTObservation;
import net.pisecurity.model.Event;
import net.pisecurity.pi.dht.DHT11;
import net.pisecurity.pi.dht.DHT11Impl.DHTReading;
import net.pisecurity.pi.persist.PersistenceService;

public class DHTMonitoringService implements Runnable {
	private static final Logger logger = LogManager.getLogger(DHTMonitoringService.class);
	private volatile boolean running;
	private Thread thread;
	private PersistenceService persistenceService;

	private DHT11 dht11;
	private String location;
	private String deviceId;

	public DHTMonitoringService(String location, PersistenceService persistenceService, DHT11 dht11, String deviceId) {
		super();
		this.persistenceService = persistenceService;
		this.dht11 = dht11;
		this.location = location;
		this.deviceId = deviceId;

		thread = new Thread(this);
		thread.setName("DHTMonitoringThread");
	}

	public void start() {
		running = true;
		thread.start();
	}

	public void shutdown() throws InterruptedException {
		if (thread != null) {
			running = false;
			thread.interrupt();
			thread.join();
		}
	}

	@Override
	public void run() {
		try {
			double sumTemp = 0;
			double sumHumidity = 0;
			int count = 0;

			while (running) {
				this.dht11.prepareRead();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return;
				}
				DHTReading readings = this.dht11.read();

				if (readings != null) {
					sumTemp += readings.temperature;
					sumHumidity += readings.humidity;
					++count;
					if (count > 60) {
						DHTObservation obs = new DHTObservation();
						obs.time = System.currentTimeMillis();
						obs.humidityPercent = sumHumidity / count;
						obs.temparatureCelcius = sumTemp / count;
						obs.location = this.location;
						obs.deviceId = this.deviceId;

						if (logger.isDebugEnabled()) {
							logger.debug("Saving : " + obs);
						}
						persistenceService.persist(obs);

						sumTemp = 0;
						sumHumidity = 0;
						count = 0;
					}
				}

			}
		} catch (Exception e) {
			logger.error("Unexpected exception while checking DHT", e);
		}
	}

}
