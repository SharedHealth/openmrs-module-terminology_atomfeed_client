package org.bahmni.module.terminology.infrastructure.atomfeed.workers;

import org.apache.log4j.Logger;
import org.bahmni.module.terminology.application.mappers.ConceptMapper;
import org.bahmni.module.terminology.application.service.ConceptRestService;
import org.bahmni.module.terminology.infrastructure.config.TRFeedProperties;
import org.bahmni.module.terminology.infrastructure.http.AuthenticatedHttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;


public class ConceptFeedWorker implements EventWorker {

    private static Logger logger = Logger.getLogger(ConceptFeedWorker.class);

    private TRFeedProperties properties;
    private ConceptRestService conceptService;
    private AuthenticatedHttpClient httpClient;

    public ConceptFeedWorker(AuthenticatedHttpClient httpClient, TRFeedProperties properties, ConceptRestService conceptService) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.conceptService = conceptService;
    }

    @Override
    public void process(final Event event) {
        logger.info(format("Received concept sync event for %s with conent %s ", event.getFeedUri(), event.getContent()));
        Map conceptMap = httpClient.get(properties.getTerminologyUrl(event.getContent()), HashMap.class);
        conceptService.save(conceptMap);
    }

    @Override
    public void cleanUp(Event event) {

    }
}