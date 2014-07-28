package org.bahmni.module.terminology.infrastructure.atomfeed.workers;

import org.apache.log4j.Logger;
import org.bahmni.module.terminology.application.service.ConceptSyncService;
import org.bahmni.module.terminology.infrastructure.config.TRFeedProperties;
import org.bahmni.module.terminology.infrastructure.http.AuthenticatedHttpClient;
import org.bahmni.module.terminology.infrastructure.mapper.ConceptRequestMapper;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;


public class ConceptFeedWorker implements EventWorker {

    private static Logger logger = Logger.getLogger(ConceptFeedWorker.class);

    private ConceptRequestMapper conceptRequestMapper;
    private TRFeedProperties properties;
    private ConceptSyncService ConceptSyncService;
    private AuthenticatedHttpClient httpClient;

    public ConceptFeedWorker(AuthenticatedHttpClient httpClient, TRFeedProperties properties, ConceptSyncService ConceptSyncService, ConceptRequestMapper conceptRequestMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.ConceptSyncService = ConceptSyncService;
        this.conceptRequestMapper = conceptRequestMapper;
    }

    @Override
    public void process(final Event event) {
        logger.info(format("Received concept sync event for %s with conent %s ", event.getFeedUri(), event.getContent()));
        Map conceptMap = httpClient.get(properties.getTerminologyUrl(event.getContent()), HashMap.class);
        ConceptSyncService.sync(conceptRequestMapper.map(conceptMap));
    }

    @Override
    public void cleanUp(Event event) {

    }
}
