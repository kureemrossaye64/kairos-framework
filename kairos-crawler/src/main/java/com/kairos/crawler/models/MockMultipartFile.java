package com.kairos.crawler.models;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple in-memory implementation of Spring's MultipartFile interface,
 * used to adapt crawler output to the IngestionRequest DTO.
 */
public class MockMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public MockMultipartFile(String originalFilename, String contentType, InputStream contentStream) throws IOException {
        this.name = "file";
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = contentStream.readAllBytes();
    }
    
    // Implement all methods of the MultipartFile interface...
    @Override public String getName() { return name; }
    @Override public String getOriginalFilename() { return originalFilename; }
    @Override public String getContentType() { return contentType; }
    @Override public boolean isEmpty() { return content == null || content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override public byte[] getBytes() { return content; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(File dest) throws IOException, IllegalStateException { /* Not needed */ }
}