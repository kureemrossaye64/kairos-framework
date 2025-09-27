package com.kairos.storage;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

public interface StorageService {
    /**
     * Uploads a file to the storage provider.
     * @param inputStream The file content.
     * @param fileName The name for the destination file.
     * @param contentType The MIME type of the file.
     * @return The public URI of the uploaded file (e.g., "gs://bucket-name/file-name").
     */
    String upload(InputStream inputStream, String fileName, String contentType);
    
    /**
     * Downloads a file from the storage provider.
     * @param fileUri The full URI of the file to download (e.g., "gs://bucket-name/file-name").
     * @return An InputStream of the file's content. The caller is responsible for closing the stream.
     */
    InputStream download(String fileUri);

    /**
     * An alternative download method that returns a channel for more efficient reading.
     * @param fileUri The full URI of the file to download.
     * @return A ReadableByteChannel for the file's content.
     */
    ReadableByteChannel downloadChannel(String fileUri);
}