package com.kairos.ingestion.processor;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.kairos.ai_abstraction.service.TextAnalysisService;
import com.kairos.ai_abstraction.service.TextAnalysisService.TextAnalysisResult;
import com.kairos.ingestion.pipeline.Processor;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiEnrichmentProcessor implements Processor<TextSegment, TextSegment> {

   private final TextAnalysisService textAnalysis;
   
   
   @Override
   public Stream<TextSegment> process(Stream<TextSegment> sourceRecordStream) {
       log.info("Applying audio transcription processor...");
       return sourceRecordStream.flatMap(segment -> {
           try {
        	   
        	   if (segment.text().isBlank()) {
                   return Stream.of(segment); // Skip empty segments
               }
        	   
        	   log.debug("Enriching segment starting with: '{}...'", segment.text().substring(0, Math.min(50, segment.text().length())));

               // Create a mutable copy of the existing metadata to preserve the user's manifest.
               Metadata newMetadata = new Metadata(segment.metadata().toMap());
               
               TextAnalysisResult result = textAnalysis.analyze(segment.text()).join();
        	   String summary = result.summary();
        	   String topics = result.topics();
        	   String questions = result.hypotheticalQuestions();
        	   
        	   newMetadata.put("summary", summary);
        	   newMetadata.put("topics", topics);
        	   newMetadata.put("hypothetical_questions", questions);
        	   

               return Stream.of( TextSegment.from(segment.text(), newMetadata));

           } catch (Exception e) {
        	   log.warn("AI enrichment failed for a text segment. Proceeding with original metadata. Error: {}", e.getMessage());
               // In case of an API error, we gracefully degrade and return the original segment.
               return Stream.of(segment);
           }
       });
   }


    
}