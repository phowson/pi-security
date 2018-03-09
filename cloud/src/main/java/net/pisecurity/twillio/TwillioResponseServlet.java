package net.pisecurity.twillio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Hangup;
import com.twilio.twiml.voice.Say;

public class TwillioResponseServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(TwillioResponseServlet.class);
	private static final long serialVersionUID = 3823780864474862531L;
	private static final long MAX_WAIT = 10000;

	private Map<String, CallStatus> calls = new HashMap<>();

	@Override
	protected synchronized void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("Saw post");
		logger.info(req.getParameterMap());
		CallStatus status;
		try {
			status = getStatus(req);

		} catch (InterruptedException e) {
			logger.error("Unexpected exception ", e);
			return;
		}

		String[] callStatusArr = req.getParameterValues("CallStatus");
		String callStatus = callStatusArr[callStatusArr.length - 1];

		logger.info("Call status = " + callStatus);
		if (callStatus.equals("in-progress")) {
			if ("Gather End".equals(req.getParameter("msg")) && "1".equals(req.getParameter("Digits"))) {
				if (status.listener != null) {
					status.listener.onCallComplete(true);
				}

				status.success = true;

				sendHangupTML(resp);
			} else {
				sendFirstMessageTML(status.message, resp);
			}
		}
	}

	@Override
	protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("Saw Get");
		logger.info(req.getParameterMap());
		String[] callStatusArr = req.getParameterValues("CallStatus");
		String callStatus = callStatusArr[callStatusArr.length - 1];

		if (callStatus.equals("in-progress")) {
			CallStatus status;
			try {
				status = getStatus(req);
				sendFirstMessageTML(status.message, resp);
			} catch (InterruptedException e) {
				logger.error("Unexpected exception ", e);
			}
		}

	}

	private CallStatus getStatus(HttpServletRequest req) throws InterruptedException {
		String[] csid = req.getParameterMap().get("CallSid");
		long startWaitTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startWaitTime < MAX_WAIT) {
			CallStatus c = calls.get(csid[0]);
			if (c == null) {
				wait(10000);
			} else {
				return c;
			}
		}
		throw new IllegalArgumentException("Never saw call " + csid[0]);
	}

	private void sendHangupTML(HttpServletResponse resp) throws IOException {
		// Create a TwiML builder object
		VoiceResponse twiml = new VoiceResponse.Builder()
				.say(new Say.Builder("Thank you. Goodbye.").voice(Say.Voice.ALICE).build())
				.hangup(new Hangup.Builder().build()).build();

		// Render TwiML as XML
		resp.setContentType("text/xml");

		try {
			resp.getWriter().print(twiml.toXml());
		} catch (TwiMLException e) {
			logger.error("Unexpected twilio exception", e);
		}
	}

	private void sendFirstMessageTML(String message, HttpServletResponse resp) throws IOException {
		// Create a TwiML builder object
		VoiceResponse twiml = new VoiceResponse.Builder()
				.say(new Say.Builder(message + ". Press 1 to acknowledge this call").voice(Say.Voice.ALICE).build())
				.gather(new Gather.Builder().numDigits(1).build()).build();

		// Render TwiML as XML
		resp.setContentType("text/xml");

		try {
			resp.getWriter().print(twiml.toXml());
		} catch (TwiMLException e) {
			logger.error("Unexpected twilio exception", e);
		}
	}

	public synchronized void onNewCall(Call call, CallStatus callStatus) {
		calls.put(call.getSid(), callStatus);
		notifyAll();
	}

}
