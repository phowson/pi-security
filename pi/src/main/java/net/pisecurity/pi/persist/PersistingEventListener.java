package net.pisecurity.pi.persist;

import java.util.logging.Logger;

import net.pisecurity.model.Event;
import net.pisecurity.pi.monitoring.EventListener;

public class PersistingEventListener implements EventListener {

	private static Logger eventLogger = Logger.getLogger("Events");
	
	private PersistenceService persistenceService;

	public PersistingEventListener(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	@Override
	public void onEvent(Event event) {
		
		
		this.persistenceService.persist(event);
		
		eventLogger.info(event.toJson());
	}

}
