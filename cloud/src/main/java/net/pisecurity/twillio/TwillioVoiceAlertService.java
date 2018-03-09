package net.pisecurity.twillio;

import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.gson.GsonBuilder;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

public class TwillioVoiceAlertService {

	private TwillioResponseServlet responseServlet;
	private TwilioAccountDetails accountDetails;
	private ServerConfig serverConfig;
	private Server server;

	public TwillioVoiceAlertService(TwilioAccountDetails accountDetails, ServerConfig serverConfig) {
		Twilio.init(accountDetails.account, accountDetails.apiKey);
		this.accountDetails = accountDetails;
		this.serverConfig = serverConfig;
		this.responseServlet = new TwillioResponseServlet();

		ServletHolder servletHolder = new ServletHolder(responseServlet);
		server = new Server(serverConfig.getPort());
		HandlerCollection coll = new HandlerCollection();
		ServletContextHandler context = new ServletContextHandler(coll, serverConfig.getPath());
		context.addServlet(servletHolder, "/*");

		server.setHandler(coll);

	}

	public void start() throws Exception {
		server.start();
	}

	public void join() throws InterruptedException {
		server.join();
	}

	public void stop() throws Exception {
		server.stop();
	}

	public void makeCall(String[] numbers, String message, CallStatusListener listener) throws URISyntaxException {

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
		TwillioVoiceAlertService service = new TwillioVoiceAlertService(details, cfg);
		service.start();

		service.makeCall(new String[] { "+447855311224" }, "Hello world", null);

		service.join();
	}
}
