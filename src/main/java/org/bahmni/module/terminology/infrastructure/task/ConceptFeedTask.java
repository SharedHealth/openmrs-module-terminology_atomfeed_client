package org.bahmni.module.terminology.infrastructure.task;


import org.bahmni.module.terminology.infrastructure.feedclient.ConceptFeedClient;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.net.URISyntaxException;

public class ConceptFeedTask extends AbstractTask {

    @Override
    public void execute() {
        ConceptFeedClient conceptFeedClient = Context.getService(ConceptFeedClient.class);
        try {
            conceptFeedClient.syncConcepts();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}