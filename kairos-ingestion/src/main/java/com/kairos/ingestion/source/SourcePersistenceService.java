package com.kairos.ingestion.source;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kairos.ingestion.events.SourceRecordCreatedEvent;
import com.kairos.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SourcePersistenceService {

    private final StorageService storageService;
    private final SourceRecordRepository sourceRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Handles Stage 1 of ingestion: Persists the raw file to cloud storage
     * and creates a database record to track its processing.
     *
     * @param file The raw file uploaded by the user.
     * @param metadataManifest The user-provided structured metadata.
     * @return The persisted SourceRecord entity.
     */
    @Transactional
    public SourceRecord persistNewSource(MultipartFile file, Map<String, Object> metadataManifest) {
        try {
            // 1. Upload to GCS to get a permanent, immutable URI.
            String uniqueFileName = "source-docs/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
            String gcsUri = storageService.upload(file.getInputStream(), uniqueFileName, file.getContentType());
            log.info("Persisted source file '{}' to GCS at {}", file.getOriginalFilename(), gcsUri);

            // 2. Create the manifest record in our database.
            SourceRecord record = new SourceRecord();
            record.setGcsUri(gcsUri);
            record.setSourceName(file.getOriginalFilename());
            record.setContentType(file.getContentType());
            record.setMetadataManifest(metadataManifest);
            record.setStatus(ProcessingStatus.QUEUED); // Mark as queued for async processing
            SourceRecord savedRecord = sourceRecordRepository.save(record);
            log.info("Created SourceRecord with ID {} for GCS URI {}", savedRecord.getId(), gcsUri);

            // 3. Publish an event to trigger the asynchronous processing pipeline (Stage 2).
            eventPublisher.publishEvent(new SourceRecordCreatedEvent(this, savedRecord.getId()));

            return savedRecord;
        } catch (Exception e) {
            log.error("Fatal error during source file persistence for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to persist source document", e);
        }
    }
}