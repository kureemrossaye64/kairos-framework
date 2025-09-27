package com.kairos.ingestion.processor;

import com.kairos.ai_abstraction.service.AudioTranscriptionService;
import com.kairos.ingestion.pipeline.Processor;
import com.kairos.ingestion.source.SourceRecord;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class AudioTranscriptionProcessor implements Processor<SourceRecord, Document> {

    private final AudioTranscriptionService transcriptionService;

    @Override
    public Stream<Document> process(Stream<SourceRecord> sourceRecordStream) {
        log.info("Applying audio transcription processor...");
        return sourceRecordStream.flatMap(record -> {
            try {
                // 1. Get the GCS URI from the source record.
                String gcsUri = record.getGcsUri();

                // 2. Submit for transcription and wait synchronously for the result.
                String transcript = transcriptionService.transcribe(gcsUri).join();
                log.info("Transcription completed for source '{}', length: {} characters.", record.getSourceName(), transcript.length());

                if (transcript.isBlank()) {
                    log.warn("Transcription for '{}' resulted in empty text. Skipping.", record.getSourceName());
                    return Stream.empty();
                }

                // 3. Create a Document object, merging the manifest with new metadata.
                Metadata metadata = new Metadata(record.getMetadataManifest());
                metadata.put("source_uri", gcsUri);
                metadata.put("source_filename", record.getSourceName());
                metadata.put("content_type", record.getContentType());
                metadata.put("processor", "AudioTranscriptionProcessor");

                return Stream.of( Document.from(transcript, metadata));

            } catch (Exception e) {
                log.error("Failed to process audio file from source record ID: {}", record.getId(), e);
                // Gracefully fail for this item by returning an empty stream.
                // The SourceRecord status will be updated to FAILED by the router.
                throw new RuntimeException("Audio transcription failed", e);
            }
        });
    }
}