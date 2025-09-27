package com.kairos.storage.gcs;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.springframework.stereotype.Service;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.kairos.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GcsStorageServiceImpl implements StorageService {

    private final Storage gcsClient; // This bean will be created by Spring Boot auto-configuration
    private final GcsProperties properties;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        String bucketName = properties.getBucketName();
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        // Use a writable channel for efficient streaming upload
        try (var writer = gcsClient.writer(blobInfo)) {
            inputStream.transferTo(Channels.newOutputStream(writer));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to GCS", e);
        }

        return String.format("gs://%s/%s", bucketName, fileName);
    }
    
    @Override
    public InputStream download(String fileUri) {
        // The channel-based method is the primary one, so we wrap it.
        return Channels.newInputStream(downloadChannel(fileUri));
    }

    @Override
    public ReadableByteChannel downloadChannel(String fileUri) {
        log.debug("Downloading file from GCS URI: {}", fileUri);
        try {
            BlobId blobId = parseGcsUri(fileUri);
            Blob blob = gcsClient.get(blobId);

            if (blob == null || !blob.exists()) {
                log.error("File not found in GCS at URI: {}", fileUri);
                throw new RuntimeException("File not found in GCS: " + fileUri);
            }

            ReadChannel reader = blob.reader();
            return reader;

        } catch (StorageException e) {
            log.error("Storage exception while trying to download from GCS URI: {}", fileUri, e);
            throw new RuntimeException("Failed to download file from GCS", e);
        }
    }

    /**
     * A helper method to safely parse a "gs://bucket/path/to/object" URI string
     * into a GCS BlobId object.
     * @param uri The GCS URI.
     * @return A valid BlobId.
     */
    private BlobId parseGcsUri(String uri) {
        if (uri == null || !uri.startsWith("gs://")) {
            throw new IllegalArgumentException("Invalid GCS URI format. Must start with 'gs://'. URI: " + uri);
        }
        
        String path = uri.substring(5); // Remove "gs://"
        int firstSlash = path.indexOf('/');
        
        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid GCS URI format. Must contain a bucket and object name. URI: " + uri);
        }
        
        String bucketName = path.substring(0, firstSlash);
        String objectName = path.substring(firstSlash + 1);

        if (objectName.isEmpty()) {
            throw new IllegalArgumentException("Invalid GCS URI format. Object name cannot be empty. URI: " + uri);
        }
        
        return BlobId.of(bucketName, objectName);
    }
}