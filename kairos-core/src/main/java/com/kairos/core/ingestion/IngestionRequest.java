package com.kairos.core.ingestion;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;

/**
 * A standard, immutable data transfer object representing a "Knowledge Packet" for ingestion.
 * It contains the raw data payload as a byte array to allow for multiple reads,
 * and a structured metadata manifest.
 */
@Getter
public class IngestionRequest {
    private final MultipartFile file;
    private final Map<String, Object> metadataManifest;

    

    private IngestionRequest(MultipartFile file, Map<String,Object> metadata) {
    	this.file = file;
    	this.metadataManifest = metadata;
    }
    
    /**
     * Factory method to create an IngestionRequest from a MultipartFile, which is the
     * most common entry point from a web controller.
     * @param file The uploaded file.
     * @param metadataManifest The user-provided metadata.
     * @return A new IngestionRequest instance.
     * @throws IOException If the file's bytes cannot be read.
     */
    public static IngestionRequest from(MultipartFile file, Map<String, Object> metadataManifest) throws IOException {
        return new IngestionRequest(file, metadataManifest);
    }

   
}