package com.kairos.ingestion.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.kairos.ingestion.pipeline.Sink;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;

import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The final destination for our RAG ingestion pipeline.
 * It takes enriched TextSegments, converts them to VdbDocuments,
 * and adds them to the search engine in batches for efficiency.
 */
@Slf4j
@RequiredArgsConstructor
public class RAGSink implements Sink<TextSegment> {

    private final VectorStoreService searchService;
    private final int batchSize;

    public RAGSink(VectorStoreService searchService) {
        this(searchService, 50); // Default batch size of 50
    }

    @Override
    public void consume(Stream<TextSegment> stream) {
        List<VdbDocument> batch = new ArrayList<>(batchSize);
        stream.forEach(segment -> {
            VdbDocument doc = VdbDocument.builder()
                .id(UUID.randomUUID()) // Each chunk gets a unique ID
                .content(segment.text())
                .metadata(segment.metadata().toMap())
                .build();
            
            batch.add(doc);

            if (batch.size() >= batchSize) {
                log.info("Submitting a batch of {} documents to the search service.", batch.size());
                searchService.addDocuments(new ArrayList<>(batch));
                batch.clear();
            }
        });

        // Submit any remaining documents in the final batch
        if (!batch.isEmpty()) {
            log.info("Submitting the final batch of {} documents.", batch.size());
            searchService.addDocuments(batch);
        }
    }
}
