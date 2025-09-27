package com.kairos.ingestion.processor;

import com.kairos.ingestion.pipeline.Processor;
import dev.langchain4j.data.document.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@Slf4j
public class TextCleaningProcessor implements Processor<Document, Document> {

    @Override
    public Stream<Document> process(Stream<Document> documentStream) {
        log.info("Applying text cleaning processor...");
        return documentStream.map(doc -> {
            String text = doc.text();
            
            // Rule 1: Normalize line endings and remove weird characters.
            text = text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            
            // Rule 2: Collapse excessive whitespace into a single space.
            text = text.replaceAll("\\s+", " ");
            
            // Rule 3: Collapse more than two consecutive newlines into exactly two.
            text = text.replaceAll("(\\n\\s*){3,}", "\n\n");
            
            // Rule 4: Trim leading/trailing whitespace from the whole document.
            text = text.trim();

            // Return a new Document, preserving the original's rich metadata.
            return  Document.from(text, doc.metadata());
        });
    }
}