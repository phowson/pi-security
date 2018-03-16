package net.pisecurity.pi.dht;

import java.util.Arrays;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

public class DHT11Impl implements DHT11 {

	public static class DHTReading {
		public float temperature;
		public float humidity;

		@Override
		public String toString() {
			return "[temperature=" + temperature + ", humidity=" + humidity + "]";
		}

	}

	// This is the only processor specific magic value, the maximum amount of
	// time to
	// spin in a loop before bailing out and considering the read a timeout.
	// This should
	// be a high value, but if you're running on a much faster platform than a
	// Raspberry
	// Pi or Beaglebone Black then it might need to be increased.
	private static final int DHT_MAXCOUNT = 32000;

	// Number of bit pulses to expect from the DHT. Note that this is 41 because
	// the first pulse is a constant 50 microsecond pulse, with 40 pulses to
	// represent
	// the data afterwards.
	private static final int DHT_PULSES = 41;

	private final int pin;
	/**
	 * Store the count that each DHT bit pulse is low and high.
	 */
	private final int[] pulseCounts = new int[DHT_PULSES * 2];
	private final int[] data = new int[5];

	public DHT11Impl(final int pin) {
		this.pin = pin;
		GpioUtil.export(pin, GpioUtil.DIRECTION_OUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pisecurity.pi.dht.DHT11#prepareRead()
	 */
	@Override
	public void prepareRead() {
		// Set pin to output.
		Gpio.pinMode(pin, Gpio.OUTPUT);

		// Set pin high for ~500 milliseconds.
		Gpio.digitalWrite(pin, Gpio.HIGH);

		// Make sure array is initialized to start at zero.
		Arrays.fill(pulseCounts, 0);
		Arrays.fill(data, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pisecurity.pi.dht.DHT11#read()
	 */
	@Override
	public DHTReading read() {

		final Thread currentThread = Thread.currentThread();
		final int priority = currentThread.getPriority();
		currentThread.setPriority(Thread.MAX_PRIORITY);
		try {
			// The next calls are timing critical and care should be taken
			// to ensure no unnecssary work is done below.

			// Set pin low for ~20 milliseconds.
			Gpio.digitalWrite(pin, Gpio.LOW);
			Gpio.delay(20);

			// Set pin at input.
			Gpio.pinMode(pin, Gpio.INPUT);
			// Need a very short delay before reading pins or else value is
			// sometimes still low.
			for (int i = 0; i < 50; ++i) {
			}

			// Wait for DHT to pull pin low.
			int count = 0;

			while (Gpio.digitalRead(pin) != 0) {
				if (++count >= DHT_MAXCOUNT) {
					// Timeout waiting for response.
					return null;
				}
			}

			// Record pulse widths for the expected result bits.
			for (int i = 0; i < DHT_PULSES * 2; i += 2) {
				// Count how long pin is low and store in pulseCounts[i]
				while (Gpio.digitalRead(pin) == 0) {
					if (++pulseCounts[i] >= DHT_MAXCOUNT) {
						// Timeout waiting for response.
						return null;
					}
				}
				// Count how long pin is high and store in pulseCounts[i+1]
				while (Gpio.digitalRead(pin) != 0) {
					if (++pulseCounts[i + 1] >= DHT_MAXCOUNT) {
						// Timeout waiting for response.
						return null;
					}
				}
			}

		} finally {
			// Drop back to normal priority.
			currentThread.setPriority(priority);
		}
		// Done with timing critical code, now interpret the results.

		// Compute the average low pulse width to use as a 50 microsecond
		// reference threshold.
		// Ignore the first two readings because they are a constant 80
		// microsecond pulse.
		int threshold = 0;
		for (int i = 2; i < DHT_PULSES * 2; i += 2) {
			threshold += pulseCounts[i];
		}
		threshold /= DHT_PULSES - 1;

		// Interpret each high pulse as a 0 or 1 by comparing it to the 50us
		// reference.
		// If the count is less than 50us it must be a ~28us 0 pulse, and if
		// it's higher
		// then it must be a ~70us 1 pulse.

		for (int i = 3; i < DHT_PULSES * 2; i += 2) {
			final int index = (i - 3) / 16;
			data[index] <<= 1;
			if (pulseCounts[i] >= threshold) {
				// One bit for long pulse.
				data[index] |= 1;
			}
			// Else zero bit for short pulse.
		}

		// Useful debug info:
		// printf("Data: 0x%x 0x%x 0x%x 0x%x 0x%x\n", data[0], data[1], data[2],
		// data[3], data[4]);

		// Verify checksum of received data.
		if (data[4] == ((data[0] + data[1] + data[2] + data[3]) & 0xFF)) {

			final DHTReading out = new DHTReading();
			// Get humidity and temp for DHT11 sensor.
			// oddly bytes 1 and 3 are only used on other models
			out.humidity = data[0];
			out.temperature = data[2];
			return out;
		} else {
			return null;
		}
	}

	public static void main(final String ars[]) throws Exception {
		if (Gpio.wiringPiSetup() == -1) {
			throw new IllegalStateException("Could not complete Gpio");
		}

		final DHT11 dht = new DHT11Impl(16);

		while (true) {
			dht.prepareRead();
			Thread.sleep(500);
			System.out.println(dht.read());
		}

	}
}