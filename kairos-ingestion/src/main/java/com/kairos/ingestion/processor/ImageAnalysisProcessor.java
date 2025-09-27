package com.kairos.ingestion.processor;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.kairos.ai_abstraction.service.ImageAnalysisService;
import com.kairos.ingestion.pipeline.Processor;
import com.kairos.ingestion.source.SourceRecord;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageAnalysisProcessor implements Processor<SourceRecord, Document>{
	
	private final ImageAnalysisService analysisService;
	
	
	@Override
    public Stream<Document> process(Stream<SourceRecord> sourceRecordStream) {
        log.info("Applying image analysis processor...");
        return sourceRecordStream.flatMap(record -> {
            try {
                // 1. Get the GCS URI from the source record.
                String gcsUri = record.getGcsUri();

                // 2. Submit for transcription and wait synchronously for the result.
                String transcript = analysisService.analyze(gcsUri).join();
                log.info("Image analysis completed for source '{}', length: {} characters.", record.getSourceName(), transcript.length());

                if (transcript.isBlank()) {
                    log.warn("image analysis for '{}' resulted in empty text. Skipping.", record.getSourceName());
                    return Stream.empty();
                }

                // 3. Create a Document object, merging the manifest with new metadata.
                Metadata metadata = new Metadata(record.getMetadataManifest());
                metadata.put("source_uri", gcsUri);
                metadata.put("source_filename", record.getSourceName());
                metadata.put("content_type", record.getContentType());
                metadata.put("processor", "ImageAnalysisProcessor");

                return Stream.of( Document.from(transcript, metadata));

            } catch (Exception e) {
                log.error("Failed to process image file from source record ID: {}", record.getId(), e);
                // Gracefully fail for this item by returning an empty stream.
                // The SourceRecord status will be updated to FAILED by the router.
                throw new RuntimeException("Image analysis failed", e);
            }
        });
    }

}
