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
import net.pisecurity.model.EventAlertType;
import net.pisecurity.model.EventType;
import net.pisecurity.twillio.CallStatusListener;
import net.pisecurity.twillio.TwilioSMS;
import net.pisecurity.twillio.TwilioVoiceAlertService;

public class NotificationService implements CallStatusListener {

	private static final Logger logger = LogManager.getLogger(NotificationService.class);

	private TwilioVoiceAlertService voiceAlertService;
	private TwilioSMS twilioSms;

	private Executor executor;

	private DatabaseReference callDbRef;
	private DatabaseReference callSequenceRef;


	public NotificationService(TwilioVoiceAlertService voiceAlertService, TwilioSMS twilioSms, Executor executor,
			DatabaseReference callDbRef, DatabaseReference callSequenceRef) {
		this.voiceAlertService = voiceAlertService;
		this.executor = executor;
		this.twilioSms = twilioSms;
		this.callDbRef = callDbRef;
		this.callSequenceRef = callSequenceRef;
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
			sendTextMessage("System disarmed", notificationConfig.notificationList);
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
			sendTextMessage("System armed", notificationConfig.notificationList);
		}
	}

	public void notifyEvents(String location, NotificationConfig notificationConfig, List<Event> events) {
		StringBuilder sb2 = new StringBuilder();
		Set<String> s = new HashSet<>();
		for (Event e : events) {

			if (e.type != EventType.ALARMTRIGGERED_AUTO && e.type != EventType.ALARMTRIGGERED_MANUAL) {
				if (s.add(e.label)) {
					sb2.append(e.label);
					sb2.append(", ");
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if (!s.isEmpty()) {
			sb.append("This is your cloud alarm system, for your intruder alarm at " + location
				+ ". I saw activity on these sensors : ");
			
			sb.append(sb2);
		} else {
			sb.append("This is your cloud alarm system, for your intruder alarm at " + location
					+ ". My alarm was triggerd. ");			
			for (Event e : events) {

				if (e.type == EventType.ALARMTRIGGERED_AUTO || e.type == EventType.ALARMTRIGGERED_MANUAL) {
					if (s.add(e.label)) {
						sb.append(e.label);
						sb.append(", ");
					}
				}
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

					callSequenceRef.runTransaction(new Transaction.Handler() {
						private long currentSequence = 1;

						public Transaction.Result doTransaction(MutableData mutableData) {

							Number n = ((Number) mutableData.getValue());
							if (n != null) {
								long seq;
								seq = n.longValue();

								obs.sequenceId = seq;
								mutableData.setValue(--seq);

								if (seq < currentSequence) {
									currentSequence = seq;
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
								}
							}

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
	public void onCallMade(String number, String message) {
		CallRecord obs = new CallRecord();
		obs.time = System.currentTimeMillis();
		obs.number = number;
		obs.label = "Call made";
		obs.message = message;

		persist(obs);
	}

	@Override
	public void onCallComplete(boolean success, String answererNumber, String message) {

		if (success) {
			CallRecord obs = new CallRecord();
			obs.time = System.currentTimeMillis();
			obs.number = answererNumber;
			obs.label = "Call answered";
			obs.message = message;
			obs.answered = true;
			persist(obs);
		}
	}

	public void notifyHeartbeatTimeout(String loc, String devName, NotificationConfig notificationConfig) {

		String message = "This is your alarm cloud monitoring service for your alarm at " + loc +". I lost connection to your alarm " + devName;
		sendTextMessage(message, notificationConfig.alarmNotificationList);
		sendVoiceMessage(message, notificationConfig.alarmNotificationList, notificationConfig.callRetries);

	}

	public void notifyReset(NotificationConfig notificationConfig) {
		sendTextMessage("Alarm reset", notificationConfig.alarmNotificationList);
	}

}
