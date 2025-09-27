package com.kairos.ingestion.rag;

import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.core.ingestion.IngestionRequest;
import com.kairos.ingestion.events.SourceRecordCreatedEvent;
import com.kairos.ingestion.pipeline.DataSource;
import com.kairos.ingestion.pipeline.Pipeline;
import com.kairos.ingestion.processor.*;
import com.kairos.ingestion.source.ProcessingStatus;
import com.kairos.ingestion.source.SourcePersistenceService;
import com.kairos.ingestion.source.SourceRecord;
import com.kairos.ingestion.source.SourceRecordRepository;
import com.kairos.storage.StorageService;
import com.kairos.vector_search.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionRouter implements IIngestionRouter{

    // --- Inject all available processors and services ---
    private final SourceRecordRepository sourceRecordRepository;
    private final VectorStoreService searchService;
    
    // Media Processors (SourceRecord -> Document)
    private final AudioTranscriptionProcessor audioProcessor;
    private final VideoAnalysisProcessor videoProcessor;
    private final ImageAnalysisProcessor imageProcessor;
    private final TikaDocumentParserProcessor tikaDocumentProcessor;
    
    // Text-Based Processors
    private final TextCleaningProcessor textCleaningProcessor;
    private final AiEnrichmentProcessor aiEnrichmentProcessor;

    private final SourcePersistenceService persistenceService;
    /**
     * The main entry point for asynchronous ingestion processing.
     * This method listens for the creation of a new SourceRecord, finds it,
     * and routes it to the correct processing pipeline.
     * @param event The event containing the ID of the new SourceRecord.
     */
    @EventListener
    @Async 
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSourceRecordCreated(SourceRecordCreatedEvent event) {
    	try {
    	Thread.sleep(3000);
    	}catch(Exception e) {
    		
    	}
    	
        SourceRecord record = sourceRecordRepository.findById(event.getSourceRecordId())
                .orElseThrow(() -> new NoSuchElementException("SourceRecord not found for ID: " + event.getSourceRecordId()));

        log.info("Starting ingestion pipeline for SourceRecord ID: {}, ContentType: {}", record.getId(), record.getContentType());

        // Mark the record as PROCESSING to prevent duplicate runs.
        record.setStatus(ProcessingStatus.PROCESSING);
        sourceRecordRepository.saveAndFlush(record);

        try {
            // The DataSource for all pipelines is the single SourceRecord we are processing.
            DataSource<SourceRecord> source = () -> java.util.stream.Stream.of(record);
            
            // The Sink is always the same: our RAGSink which indexes the final text segments.
            RAGSink sink = new RAGSink(searchService);

            // --- THE CORE ROUTING LOGIC ---
            String contentType = record.getContentType() != null ? record.getContentType().toLowerCase() : "";

            if (contentType.startsWith("audio/")) {
                log.info("Routing to AUDIO pipeline for SourceRecord ID: {}", record.getId());
                Pipeline.from(source)
                    .through(audioProcessor)      // SourceRecord -> Transcribed Document
                    .through(textCleaningProcessor)   // Clean the transcript
                    .through(new DocumentSplitterProcessor(500, 50))
                    .through(aiEnrichmentProcessor) // Enrich the transcript chunks
                    .to(sink);

            } else if (contentType.startsWith("video/")) {
                log.info("Routing to VIDEO pipeline for SourceRecord ID: {}", record.getId());
                Pipeline.from(source)
                    .through(videoProcessor)      // SourceRecord -> Analyzed Document (transcript + narrative)
                    .through(textCleaningProcessor)
                    .through(new DocumentSplitterProcessor(1000, 100)) // Use larger chunks for video
                    .through(aiEnrichmentProcessor)
                    .to(sink);

            } else if (contentType.startsWith("image/")) {
                log.info("Routing to IMAGE pipeline for SourceRecord ID: {}", record.getId());
                Pipeline.from(source)
                    .through(imageProcessor)  
                    
                    // SourceRecord -> Described Document
                    // Image descriptions are usually short and dense.
                    // We'll enrich them but not split them.
                    .through(new DocumentSplitterProcessor(500, 50))
                    .through(aiEnrichmentProcessor)
                    .to(sink);

            } else if (isDocumentType(contentType)) {
                log.info("Routing to DOCUMENT pipeline for SourceRecord ID: {}", record.getId());
                Pipeline.from(source)
                    .through(tikaDocumentProcessor) // SourceRecord -> Parsed Document
                    .through(textCleaningProcessor)
                    .through(new DocumentSplitterProcessor(500, 50))
                    .through(aiEnrichmentProcessor)
                    .to(sink);

            } else {
                log.warn("Unknown or unsupported content type: '{}' for SourceRecord ID: {}. No specific pipeline found.", contentType, record.getId());
                // We can choose to have a default fallback or mark as failed.
                // Let's mark as FAILED for clarity.
                throw new UnsupportedOperationException("Unsupported content type: " + contentType);
            }
            
            // If the pipeline executes without throwing an exception, mark as completed.
            record.setStatus(ProcessingStatus.COMPLETED);
            log.info("Successfully completed ingestion pipeline for SourceRecord ID: {}", record.getId());

        } catch (Exception e) {
            log.error("Ingestion pipeline FAILED for SourceRecord ID: {}", record.getId(), e);
            record.setStatus(ProcessingStatus.FAILED);
            record.setFailureReason(e.getMessage());
        } finally {
            sourceRecordRepository.saveAndFlush(record);
        }
    }

    /**
     * A helper method to identify common document MIME types.
     */
    private boolean isDocumentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("text/plain") ||
               contentType.equals("text/csv") ||
               contentType.equals("text/html") ||
               contentType.contains("officedocument"); // Catches .docx, .xlsx, etc.
    }

	@Override
	public void addRequest(IngestionRequest request) {
		
		persistenceService.persistNewSource(request.getFile(), request.getMetadataManifest());
		// TODO Auto-generated method stub
		
	}
}