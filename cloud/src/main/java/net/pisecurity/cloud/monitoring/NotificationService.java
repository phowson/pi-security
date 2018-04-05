package net.pisecurity.cloud.monitoring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import net.pisecurity.cloud.model.CallRecord;
import net.pisecurity.cloud.model.NotificationConfig;
import net.pisecurity.cloud.model.PhoneRecord;
import net.pisecurity.model.Event;
import net.pisecurity.twillio.CallStatusListener;
import net.pisecurity.twillio.TwilioSMS;
import net.pisecurity.twillio.TwilioVoiceAlertService;

public class NotificationService implements CallStatusListener {

	private static final Logger logger = LogManager.getLogger(NotificationService.class);

	private TwilioVoiceAlertService voiceAlertService;
	private TwilioSMS twilioSms;

	private Executor executor;

	private DatabaseReference callDbRef;

	public NotificationService(TwilioVoiceAlertService voiceAlertService, TwilioSMS twilioSms, Executor executor,
			DatabaseReference callDbRef) {
		this.voiceAlertService = voiceAlertService;
		this.executor = executor;
		this.twilioSms = twilioSms;
		this.callDbRef = callDbRef;
	}

	private void sendVoiceMessage(String s, List<PhoneRecord> numbersToCall, int callRetries) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					sendVoiceMessageImpl(s, numbersToCall, callRetries);
				} catch (Exception e) {
					logger.error("Unexpected exception", e);
				}
			}
		});
	}

	private void sendTextMessage(String s, List<PhoneRecord> numbersToCall) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					sendTextMessageImpl(s, numbersToCall);
				} catch (Exception e) {
					logger.error("Unexpected exception", e);
				}
			}
		});
	}

	protected void sendVoiceMessageImpl(String text, List<PhoneRecord> numbersToCall, int callRetries)
			throws ClientProtocolException, URISyntaxException, IOException {

		String[] numbers = new String[numbersToCall.size() * callRetries];

		int idx = 0;
		for (int i = 0; i < numbersToCall.size(); ++i) {
			for (int j = 0; j < callRetries; ++j) {
				numbers[idx++] = numbersToCall.get(i).number;
			}

		}

		this.voiceAlertService.makeCall(numbers, text, false, this);

	}

	protected void sendTextMessageImpl(String text, List<PhoneRecord> numbersToCall)
			throws ClientProtocolException, IOException, URISyntaxException {

		String[] numbers = new String[numbersToCall.size()];

		for (int i = 0; i < numbers.length; ++i) {
			numbers[i] = numbersToCall.get(i).number;
		}

		this.twilioSms.sendSms(numbers, text);

	}

	public void notifyAutoDisarm(NotificationConfig notificationConfig) {

		if (notificationConfig.sendTextsOnArmDisarm) {
			sendTextMessage("System automatically disarmed", notificationConfig.notificationList);
		}
	}

	public void reportPinActivity(Event event, NotificationConfig notificationConfig) {

		if (notificationConfig.sendTextsForNotification) {
			String message = "Saw activity on : " + event.label;
			sendTextMessage(message, notificationConfig.notificationList);
		}

	}

	public void notifyAutoArm(NotificationConfig notificationConfig) {
		if (notificationConfig.sendTextsOnArmDisarm) {
			sendTextMessage("System automatically armed", notificationConfig.notificationList);
		}
	}

	public void notifyEvents(NotificationConfig notificationConfig, List<Event> events) {
		StringBuilder sb = new StringBuilder();

		sb.append("Saw activity in the following areas : ");

		Set<String> s = new HashSet<>();
		for (Event e : events) {

			if (s.add(e.label)) {
				sb.append(e.label);
				sb.append(", ");
			}
		}

		if (notificationConfig.sendTextsForAlarm) {
			sendTextMessage(sb.toString(), notificationConfig.alarmNotificationList);
		}

		sendVoiceMessage(sb.toString(), notificationConfig.alarmNotificationList, notificationConfig.callRetries);

	}

	private void persist(CallRecord obs) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {

					callDbRef.push().runTransaction(new Transaction.Handler() {
						public Transaction.Result doTransaction(MutableData mutableData) {
							mutableData.setValue(obs);
							return Transaction.success(mutableData);
						}

						public void onComplete(DatabaseError databaseError, boolean complete,
								DataSnapshot dataSnapshot) {
							if (databaseError == null && complete) {
								if (logger.isDebugEnabled()) {
									logger.debug("observation persisted OK");
								}
							}
						}
					});

				} catch (Exception e) {
					logger.error("Unexpected exception", e);
				}
			}
		});

	}

	@Override
	public void onCallMade(String number) {
		CallRecord obs = new CallRecord();
		obs.time = System.currentTimeMillis();
		obs.number = number;
		obs.label = "Call made";

		persist(obs);
	}

	@Override
	public void onCallComplete(boolean success, String answererNumber) {

		if (success) {
			CallRecord obs = new CallRecord();
			obs.time = System.currentTimeMillis();
			obs.number = answererNumber;
			obs.label = "Call answered";
			obs.answered = true;
			persist(obs);
		}
	}

	public void notifyHeartbeatTimeout(String s, NotificationConfig notificationConfig) {
		sendTextMessage("Internet connection lost to the following location : " + s,
				notificationConfig.notificationList);

	}

}
