package net.pisecurity.twillio;

import java.io.FileReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.gson.GsonBuilder;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

import net.pisecurity.util.NamedThreadFactory;

public class TwilioVoiceAlertService implements UncaughtExceptionHandler, Runnable {

	private static final Logger logger = LogManager.getLogger(TwilioVoiceAlertService.class);
	private TwilioResponseServlet responseServlet;
	private TwilioAccountDetails accountDetails;
	private ServerConfig serverConfig;
	private Server server;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> future;

	public TwilioVoiceAlertService(TwilioAccountDetails accountDetails, ServerConfig serverConfig) {
		Twilio.init(accountDetails.account, accountDetails.apiKey);
		this.accountDetails = accountDetails;
		this.serverConfig = serverConfig;
		this.responseServlet = new TwilioResponseServlet();

		ServletHolder servletHolder = new ServletHolder(responseServlet);
		server = new Server(serverConfig.getPort());
		HandlerCollection coll = new HandlerCollection();
		ServletContextHandler context = new ServletContextHandler(coll, serverConfig.getPath());
		context.addServlet(servletHolder, "/*");

		server.setHandler(coll);

		scheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("TwilioTimeout", this, false));
	}

	public void start() throws Exception {
		server.start();
		future = scheduler.scheduleWithFixedDelay(this, TwilioResponseServlet.TIMEOUT_TIME / 2,
				TwilioResponseServlet.TIMEOUT_TIME / 2, TimeUnit.MILLISECONDS);
	}

	public void join() throws InterruptedException {
		server.join();
		future.cancel(true);
	}

	public void stop() throws Exception {
		server.stop();
		future.cancel(true);
	}

	public void makeCall(String[] numbers, String message, CallStatusListener listener) throws URISyntaxException {
		logger.info("Starting call. Will try " + Arrays.toString(numbers) + " with message " + message);
		CallStatus callStatus = new CallStatus(System.currentTimeMillis(), numbers, message, listener, 0);
		doCall(numbers[0], callStatus);

	}

	private void doCall(String string, CallStatus callStatus) throws URISyntaxException {

		Call call = Call.creator(new PhoneNumber(string), new PhoneNumber(accountDetails.phoneNumber),
				new URI(serverConfig.getMyUrl())).create();

		responseServlet.onNewCall(call, callStatus);

	}

	public static void main(String[] args) throws Exception {
		GsonBuilder builder = new GsonBuilder();
		// 86.181.62.37
		TwilioAccountDetails details;
		ServerConfig cfg;
		try (FileReader r = new FileReader("./resources/config/twillio.prod.json");) {
			details = builder.create().fromJson(r, TwilioAccountDetails.class);
		}

		try (FileReader r = new FileReader("./resources/config/serverconfig.dev.json");) {
			cfg = builder.create().fromJson(r, ServerConfig.class);
		}
		TwilioVoiceAlertService service = new TwilioVoiceAlertService(details, cfg);
		service.start();

		service.makeCall(new String[] { "+447855311224", "+447855311224" }, "Hello world", new CallStatusListener() {

			@Override
			public void onCallComplete(boolean success) {
				logger.info("Saw call complete = " + success);
			}
		});

		service.join();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught exception on thread " + t, e);
	}

	@Override
	public void run() {

		synchronized (this.responseServlet) {
			this.responseServlet.cleanOldCalls();
			List<CallStatus> cs = this.responseServlet.getTimedOutCalls();

			for (CallStatus c : cs) {
				if (!c.notified) {

					c.notified = true;

					if (c.index < c.numbers.length - 1) {
						try {
							String n = c.numbers[c.index + 1];
							logger.info("Call failed, trying next number : " + n);
							doCall(n, new CallStatus(System.currentTimeMillis(), c.numbers, c.message, c.listener,
									c.index + 1));
						} catch (URISyntaxException e) {
							logger.error("Unexpected exception", e);
						}
					} else {
						try {
							c.listener.onCallComplete(false);
						} catch (Exception e) {
							logger.error("Unexpected exception", e);
						}
					}

				}
			}

		}
	}
}
