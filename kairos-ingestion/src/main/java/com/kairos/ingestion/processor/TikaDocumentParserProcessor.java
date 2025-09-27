package com.kairos.ingestion.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import com.kairos.ingestion.pipeline.Processor;
import com.kairos.ingestion.source.SourceRecord;
import com.kairos.storage.StorageService;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A Processor that uses Apache Tika to parse various file types (PDF, DOCX, etc.)
 * and extracts their text content into a LangChain4j Document.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TikaDocumentParserProcessor implements Processor<SourceRecord, Document> {

    private final Tika tika = new Tika();
    
    private final StorageService storageService;

    

    @Override
    public Stream<Document> process(Stream<SourceRecord> requestStream) {
        log.info("Applying Tika document parser processor...");
        return requestStream.map(request -> {
        	try (InputStream is = storageService.download(request.getGcsUri())) {
                // 1. Create the initial Metadata object from the user-provided manifest.
                Metadata metadata = new Metadata(request.getMetadataManifest());
                
                // 2. Add system-generated metadata.
                metadata.put("source_filename", request.getSourceName());
                metadata.put("content_type", request.getContentType());
                metadata.put("parser", "Tika");
                
                // 3. Parse the document content.
                String content = tika.parseToString(is);

                return Document.from(content, metadata);
            } catch (IOException | TikaException e) {
                log.error("Failed to parse document with Tika (source: {}).", request.getSourceName(), e);
                return null;
            }
        }).filter(java.util.Objects::nonNull);
    }
}