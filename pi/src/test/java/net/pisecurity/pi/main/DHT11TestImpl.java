package net.pisecurity.pi.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.pisecurity.pi.dht.DHT11;
import net.pisecurity.pi.dht.DHT11Impl.DHTReading;

public class DHT11TestImpl implements DHT11 {

	private static final Logger logger = LogManager.getLogger(DHT11TestImpl.class);

	@Override
	public void prepareRead() {

	}

	@Override
	public DHTReading read() {
		DHTReading r = new DHTReading();
		r.humidity = (float) (30 + (5 * Math.random()));
		r.temperature = (float) (22+ (5 * Math.random()));
		return r;
	}
}
