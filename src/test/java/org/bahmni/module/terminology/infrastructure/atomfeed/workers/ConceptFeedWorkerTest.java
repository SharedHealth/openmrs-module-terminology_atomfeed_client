package org.bahmni.module.terminology.infrastructure.atomfeed.workers;

import org.bahmni.module.terminology.application.mappers.BasicConceptMapper;
import org.bahmni.module.terminology.infrastructure.atomfeed.postprocessors.NOPPostProcessor;
import org.bahmni.module.terminology.infrastructure.config.TRFeedProperties;
import org.bahmni.module.terminology.infrastructure.http.AuthenticatedHttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConceptFeedWorkerTest {

    public static final String CONCEPT_BASE_URL = "http://localhost/";

    private ArgumentCaptor<Concept> argumentCaptor = ArgumentCaptor.forClass(Concept.class);

    @Mock
    private ConceptService conceptService;

    @Mock
    private BasicConceptMapper mapper;

    @Mock
    private AuthenticatedHttpClient httpClient;

    @Mock
    private TRFeedProperties properties;

    private ConceptFeedWorker conceptFeedWorker;

    private Event event;

    @Before
    public void setup() {
        initMocks(this);
        event = new Event("eventId", "/content", "title", "feedUri");
        properties = createProperties();
        conceptFeedWorker = new ConceptFeedWorker(httpClient, properties, conceptService, mapper, new NOPPostProcessor());
    }

    private TRFeedProperties createProperties() {
        Properties feedDefaults = new Properties();
        feedDefaults.setProperty(TRFeedProperties.TERMINOLOGY_FEED_URI, CONCEPT_BASE_URL);
        return new TRFeedProperties(feedDefaults);
    }

    @Test
    public void shouldSaveTheConceptFetched() throws IOException {
        HashMap<String, Object> response = new HashMap<String, Object>();
        Concept concept = mock(Concept.class);
        when(concept.getName()).thenReturn(new ConceptName("concept", Locale.CANADA));

        when(mapper.map(response)).thenReturn(concept);
        when(httpClient.get("http://localhost/content", HashMap.class)).thenReturn(response);
        when(conceptService.getConceptByName(anyString())).thenReturn(null);

        conceptFeedWorker.process(event);

        verify(conceptService, times(1)).saveConcept(argumentCaptor.capture());
        assertEquals(concept, argumentCaptor.getValue());
    }

    @Test
    public void shouldNotTryToSaveConceptThatIsSavedAlready() {
        HashMap<String, Object> response = new HashMap<String, Object>();
        Concept concept = mock(Concept.class);
        when(concept.getName()).thenReturn(new ConceptName("concept", Locale.CANADA));

        when(mapper.map(response)).thenReturn(concept);
        when(httpClient.get("http://localhost/content", HashMap.class)).thenReturn(response);
        when(conceptService.getConceptByName(anyString())).thenReturn(new Concept());

        conceptFeedWorker.process(event);
        verify(conceptService, never()).saveConcept(any(Concept.class));
    }
}