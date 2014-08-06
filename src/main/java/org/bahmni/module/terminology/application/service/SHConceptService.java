package org.bahmni.module.terminology.application.service;

import org.bahmni.module.terminology.application.mapping.ConceptMapper;
import org.bahmni.module.terminology.application.model.ConceptRequest;
import org.bahmni.module.terminology.infrastructure.mapper.ConceptUpdate;
import org.bahmni.module.terminology.application.model.IdMapping;
import org.bahmni.module.terminology.application.postprocessor.PostProcessorFactory;
import org.bahmni.module.terminology.infrastructure.atomfeed.postprocessors.ConceptPostProcessor;
import org.bahmni.module.terminology.infrastructure.repository.IdMappingsRepository;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.bahmni.module.terminology.application.model.TerminologyClientConstants.CONCEPT;

@Component
public class SHConceptService {

    @Autowired
    private PostProcessorFactory postProcessorFactory;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private IdMappingsRepository idMappingsRepository;
    @Autowired
    private ConceptMapper conceptMapper;
    @Autowired
    private ConceptUpdate conceptUpdate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sync(ConceptRequest conceptRequest) {
        IdMapping idMapping = idMappingsRepository.findByExternalId(conceptRequest.getUuid());
        Concept newConcept = conceptMapper.map(conceptRequest);
        if (idMapping == null) {
            Concept savedConcept = conceptService.saveConcept(newConcept);
            ConceptPostProcessor postProcessor = postProcessorFactory.getPostProcessor(savedConcept);
            if (null != postProcessor) {
                postProcessor.process(savedConcept);
            }
            idMappingsRepository.saveMapping(new IdMapping(savedConcept.getUuid(), conceptRequest.getUuid(), CONCEPT, conceptRequest.getUri()));
        } else {
            Concept existingConcept = conceptService.getConceptByUuid(idMapping.getInternalId());
            conceptService.saveConcept(
                    conceptUpdate.mergeSpecifics(
                            conceptService.saveConcept(conceptUpdate.merge(newConcept, existingConcept)),
                            newConcept
                    )
            );
        }
    }
}
