package net.pisecurity.pi.main;

import net.pisecurity.pi.dht.DHT11;
import net.pisecurity.pi.dht.DHT11Factory;

public class DHT11TestFactoryImpl implements DHT11Factory {

	@Override
	public DHT11 create(int pin) {
		return new DHT11TestImpl();
	}
}
