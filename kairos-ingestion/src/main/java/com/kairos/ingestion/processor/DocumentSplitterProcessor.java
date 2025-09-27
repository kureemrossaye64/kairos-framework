package com.kairos.ingestion.processor;

import com.kairos.ingestion.pipeline.Processor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Stream;

/**
 * A Processor that takes LangChain4j Documents and splits them into smaller TextSegments.
 * This is a crucial step before embedding, as it ensures the text chunks fit within the
 * context window of the embedding model and improves retrieval relevance.
 */
@Slf4j
public class DocumentSplitterProcessor implements Processor<Document, TextSegment> {

    private final DocumentSplitter splitter;

    public DocumentSplitterProcessor(int maxSegmentSize, int maxOverlap) {
        // Using a built-in LangChain4j splitter. We can customize this easily.
        this.splitter = DocumentSplitters.recursive(maxSegmentSize, maxOverlap);
    }

    @Override
    public Stream<TextSegment> process(Stream<Document> inputStream) {
        log.info("Applying document splitting processor...");
        // flatMap is used because one document can be split into multiple segments.
        return inputStream.flatMap(document -> {
            log.debug("Splitting document with metadata: {}", document.metadata());
            return splitter.split(document).stream();
        });
    }
}