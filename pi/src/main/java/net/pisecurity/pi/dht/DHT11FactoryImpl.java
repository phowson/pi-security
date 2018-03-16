package net.pisecurity.pi.dht;

public class DHT11FactoryImpl implements DHT11Factory {

	@Override
	public DHT11 create(int pin) {
		return new DHT11Impl(pin);
	}
}
