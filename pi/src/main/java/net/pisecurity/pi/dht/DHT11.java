package net.pisecurity.pi.dht;

import net.pisecurity.pi.dht.DHT11Impl.DHTReading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DHT11 {

	/**
	 * Call before every read
	 */
	void prepareRead();

	/**
	 * Call around 500 milliseconds after prepareRead
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	DHTReading read() ;

}