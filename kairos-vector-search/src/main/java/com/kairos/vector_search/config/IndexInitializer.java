package com.kairos.vector_search.config;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.kairos.search.annotation.Searchable;
import com.kairos.vector_search.service.VectorStoreService;

//@Component
class IndexInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    @Autowired
    private VectorStoreService openSearchIndexService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // Use Reflections to find all classes annotated with @Searchable
            Reflections reflections = new Reflections("com.kairos");  // Replace "com.kairos" with your base package
            Set<Class<?>> searchableClasses = reflections.getTypesAnnotatedWith(Searchable.class);

            log.info("Found {} classes annotated with @Searchable", searchableClasses.size());

            // Create the index for each searchable class
            for (Class<?> searchableClass : searchableClasses) {
                try {
                    log.info("Creating index for class: {}", searchableClass.getName());
                    openSearchIndexService.createIndexWithMapping(searchableClass);
                } catch (Exception e) {
                    log.error("Failed to create index for class: {}", searchableClass.getName(), e);
                    // Consider whether you want to continue if one index fails.
                }
            }

            log.info("Finished creating OpenSearch indices.");

        } catch (Exception e) {
            log.error("Error during OpenSearch index initialization", e);
        }
    }
}