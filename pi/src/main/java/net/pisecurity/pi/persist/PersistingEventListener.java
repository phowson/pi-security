package net.pisecurity.pi.persist;

import org.apache.logging.log4j.LogManager;

import net.pisecurity.model.Event;
import net.pisecurity.pi.monitoring.EventListener;

public class PersistingEventListener implements EventListener {

	private static final org.apache.logging.log4j.Logger eventLogger = LogManager.getLogger("EventMessages");

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
