package net.pisecurity.util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import net.pisecurity.model.Event;
import net.pisecurity.model.EventPersistenceService;

public class FirebaseEventPersistenceService implements EventPersistenceService {

	private static final Logger logger = LogManager.getLogger(EventPersistenceService.class);

	private final DatabaseReference eventsSequenceRef;
	private final DatabaseReference eventsRef;

	
	
	

	public FirebaseEventPersistenceService(DatabaseReference eventsSequenceRef, DatabaseReference eventsRef) {
		super();
		this.eventsSequenceRef = eventsSequenceRef;
		this.eventsRef = eventsRef;
	}

	@Override
	public void persist(Event event) {
		persistInternal(event, 0);

	}

	private void persistInternal(Event event, int retries) {

		eventsSequenceRef.runTransaction(new Transaction.Handler() {
			private long currentSequence = 1;

			public Transaction.Result doTransaction(MutableData mutableData) {

				Number n = ((Number) mutableData.getValue());
				if (n != null) {
					long seq;
					seq = n.longValue();

					event.sequenceId = seq;
					mutableData.setValue(--seq);

					if (seq < currentSequence) {
						currentSequence = seq;

						eventsRef.push().setValue(event, new CompletionListener() {

							@Override
							public void onComplete(DatabaseError databaseError, DatabaseReference ref) {
								if (databaseError != null) {
									logger.error("Error reported during save of event. Cannot continue" + databaseError
											+ ", retries = " + retries);

								}
							}
						});
					}

				}
				return Transaction.success(mutableData);

			}

			@Override
			public void onComplete(DatabaseError databaseError, boolean complete, DataSnapshot currentData) {
				if (databaseError != null ) {
					logger.error("Error reported during save of event. Cannot continue" + databaseError
							+ ", complete = " + complete + ", retries = " + retries);

				}
			}
		}, false);

	}

	
}
