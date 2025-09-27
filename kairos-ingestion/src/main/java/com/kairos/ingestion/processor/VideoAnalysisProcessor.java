package com.kairos.ingestion.processor;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.kairos.ai_abstraction.service.VideoAnalysisService;
import com.kairos.ai_abstraction.service.VideoAnalysisService.VideoAnalysisResult;
import com.kairos.ingestion.pipeline.Processor;
import com.kairos.ingestion.source.SourceRecord;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class VideoAnalysisProcessor implements Processor<SourceRecord, Document> {

    private final VideoAnalysisService videoAnalysisService;

    @Override
    public Stream<Document> process(Stream<SourceRecord> sourceRecordStream) {
        log.info("Applying video analysis processor...");
        return sourceRecordStream.flatMap(record -> {
            try {
                String gcsUri = record.getGcsUri();
                VideoAnalysisResult result = videoAnalysisService.analyze(gcsUri).join();

                // Combine transcript and narrative into one rich text document for embedding.
                String combinedText = "--- Audio Transcript ---\n" + result.transcript() +
                                      "\n\n--- Visual Narrative ---\n" + result.visualNarrative();

                Metadata metadata = new Metadata(record.getMetadataManifest());
                metadata.put("source_uri", gcsUri);
                metadata.put("source_filename", record.getSourceName());
                metadata.put("content_type", record.getContentType());
                metadata.put("processor", "VideoAnalysisProcessor");
                metadata.put("has_visual_narrative", "yes");

                return Stream.of( Document.from(combinedText, metadata));
            } catch (Exception e) {
                log.error("Failed to process video file from source record ID: {}", record.getId(), e);
                throw new RuntimeException("Video analysis failed", e);
            }
        });
    }
}