package com.kairos.crawler;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.core.ingestion.IngestionRequest;
import com.kairos.crawler.config.RabbitMQConfig;
import com.kairos.crawler.entities.CrawledUrl;
import com.kairos.crawler.models.DocumentExtraction;
import com.kairos.crawler.models.MockMultipartFile;
import com.kairos.crawler.models.UrlToCrawl;
import com.kairos.crawler.repositories.CrawledUrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerWorker {

    private final WebCrawlerService webCrawlerService;
    private final CrawledUrlRepository crawledUrlRepository;
    private final IIngestionRouter ingestionRouter;
    private final YouTubeService youtubeService;

    @RabbitListener(queues = RabbitMQConfig.URL_QUEUE_NAME)
    @Transactional
    public void processUrl(UrlToCrawl urlToCrawl) {
        log.info("Worker received message to crawl: {}", urlToCrawl);

        // 1. Check if URL has been crawled recently to prevent duplicate work.
        if (crawledUrlRepository.existsByUrlHash(urlToCrawl.url().hashCode())) {
            log.debug("URL {} has already been crawled recently. Acknowledging and skipping.", urlToCrawl.url());
            return;
        }

        // --- YouTube Specialization (Example of Extensibility) ---
        if (isYouTubeUrl(urlToCrawl.url())) {
           
        	
        	
        	log.info("YouTube URL detected. Delegating to YouTubeService.");
            DocumentExtraction extraction = youtubeService.extractVideoDetails(urlToCrawl.url());
            
            if (extraction != null) {
                // If we got data, submit it to our standard ingestion pipeline.
                try {
                    Map<String, Object> manifest = new HashMap<>();
                    // The YouTube service already provides rich metadata.
                    // We just need to convert the List<String> values to Object.
                    extraction.getMetadata().forEach(manifest::put);

                    IngestionRequest request = IngestionRequest.from(
                        new MockMultipartFile(
                            extraction.getReference(),
                            "text/plain", // The content is now plain text (title + desc)
                            new ByteArrayInputStream(extraction.getContent().getBytes(StandardCharsets.UTF_8))
                        ),
                        manifest
                    );
                    
                    ingestionRouter.addRequest(request);
                    log.info("Submitted YouTube video details from {} to ingestion pipeline.", extraction.getReference());
                } catch (Exception e) {
                     log.error("Failed to submit YouTube extraction to ingestion pipeline for URL: {}", urlToCrawl.url(), e);
                }
            }
        	
        	
            crawledUrlRepository.save(new CrawledUrl(urlToCrawl.url()));
            return;
        }

        // --- Generic Web Crawl ---
        try {
            // 2. Execute the crawl for this specific URL.
            List<DocumentExtraction> extractions = webCrawlerService.executeCrawl(urlToCrawl.url(), urlToCrawl.depth());

            // 3. For each extracted document, submit it to our ingestion pipeline.
            for (DocumentExtraction doc : extractions) {
                // The crawler becomes a client of our ingestion framework.
                Map<String, Object> manifest = new HashMap<>();
                manifest.put("source_crawler_url", doc.getReference());
                manifest.put("document_title", doc.getMetadata().getOrDefault("title", List.of("Untitled")).get(0));

                // Convert the extracted string content back to an InputStream for the IngestionRequest
                InputStream contentStream = new ByteArrayInputStream(doc.getContent().getBytes(StandardCharsets.UTF_8));
                String contentType = doc.getMetadata().getOrDefault("Content-Type", List.of("text/html")).get(0);
                
                // We create a mock MultipartFile to satisfy the IngestionRequest contract.
                // This is a clean way to bridge the two systems.
                IngestionRequest request = IngestionRequest.from(
                    new MockMultipartFile(doc.getReference(), contentType, contentStream),
                    manifest
                );
                
                ingestionRouter.addRequest(request);
                log.info("Submitted document from {} to ingestion pipeline.", doc.getReference());
            }

            // 4. Mark the original URL as crawled in our database.
            crawledUrlRepository.save(new CrawledUrl(urlToCrawl.url()));

        } catch (Exception e) {
            log.error("Crawl failed for URL: {}. Message will be requeued if possible.", urlToCrawl.url(), e);
            // By throwing an exception, we signal to RabbitMQ that processing failed.
            // Depending on configuration, it may retry or move to a dead-letter queue.
            throw new RuntimeException("Crawl failed", e);
        }
    }

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }
}