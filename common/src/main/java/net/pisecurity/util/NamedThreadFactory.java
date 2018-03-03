package net.pisecurity.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

	private final String name;
	private final UncaughtExceptionHandler eh;
	private boolean daemon;

	public NamedThreadFactory(String name, UncaughtExceptionHandler eh, boolean daemon) {
		this.name = name;
		this.eh = eh;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t =new Thread(r, name);
		t.setUncaughtExceptionHandler(eh);
		t.setDaemon(daemon);
		return t;
	}

}
