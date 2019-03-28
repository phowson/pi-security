package net.pisecurity.twillio;

import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
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
	private TwilioSMS twilioSMS;

	public TwilioVoiceAlertService(TwilioAccountDetails accountDetails, ServerConfig serverConfig) {
		Twilio.init(accountDetails.account, accountDetails.apiKey);
		this.accountDetails = accountDetails;
		this.serverConfig = serverConfig;
		this.responseServlet = new TwilioResponseServlet();
		this.twilioSMS = new TwilioSMS(accountDetails);
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

	public void makeCall(String[] numbers, String message, boolean sendSms, CallStatusListener listener)
			throws URISyntaxException, ClientProtocolException, IOException {

		logger.info("Starting call. Will try " + Arrays.toString(numbers) + " with message " + message);
		CallStatus callStatus = new CallStatus(System.currentTimeMillis(), numbers, message, listener, 0, sendSms);
		doCall(numbers[0], callStatus);

	}

	private void doCall(String number, CallStatus callStatus)
			throws URISyntaxException, ClientProtocolException, IOException {
		if (callStatus.listener != null) {
			try {
				callStatus.listener.onCallMade(number, callStatus.message);
			} catch (Exception e) {
				logger.error("Unexpected exception", e);
			}
		}
		if (callStatus.sendSms) {
			twilioSMS.sendSms(new String[] { number }, callStatus.message);
		}

		Call call = Call.creator(new PhoneNumber(number), new PhoneNumber(accountDetails.phoneNumber),
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

		service.makeCall(new String[] { "+447855311224", "+447855311224" }, "Hello world", true,
				new CallStatusListener() {

					@Override
					public void onCallComplete(boolean success, String answererNumber, String message) {
						logger.info("Saw call complete = " + success + " number = " + answererNumber);
					}

					@Override
					public void onCallMade(String number, String message) {

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
									c.index + 1, c.sendSms));
						} catch (Exception e) {
							logger.error("Unexpected exception", e);
						}
					} else {
						try {
							String n = c.numbers[c.index + 1];
							c.listener.onCallComplete(false,n, c.message);
						} catch (Exception e) {
							logger.error("Unexpected exception", e);
						}
					}

				}
			}

		}
	}
}
