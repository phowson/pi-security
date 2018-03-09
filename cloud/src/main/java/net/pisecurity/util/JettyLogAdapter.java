package net.pisecurity.util;

import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Logger;

public class JettyLogAdapter extends AbstractLogger {
	private String name;
	private org.apache.logging.log4j.Logger logger;

	public JettyLogAdapter() {
		this.name = this.getClass().getName();
		this.logger = LogManager.getLogger(name);

	}

	public JettyLogAdapter(String name) {
		this.name = name;
		this.logger = LogManager.getLogger(name);
	}

	@Override
	public void debug(Throwable arg0) {
		logger.debug(arg0);
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		logger.debug(arg0, arg1);
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		logger.debug(arg0, arg1);

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void ignore(Throwable arg0) {
	}

	@Override
	public void info(Throwable arg0) {
		logger.info(arg0);
	}

	@Override
	public void info(String arg0, Object... arg1) {
		logger.info(arg0, arg1);
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		logger.info(arg0, arg1);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void setDebugEnabled(boolean arg0) {
	}

	@Override
	public void warn(Throwable arg0) {
		logger.warn(arg0);
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		logger.warn(arg0, arg1);
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		logger.warn(arg0, arg1);
	}

	@Override
	protected Logger newLogger(String arg0) {
		return new JettyLogAdapter(arg0);
	}

}
