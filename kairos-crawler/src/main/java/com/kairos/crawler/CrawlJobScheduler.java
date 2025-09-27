// src/main/java/com/kairos/sports_atlas/crawler/CrawlJobScheduler.java
package com.kairos.crawler;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.crawler.config.RabbitMQConfig;
import com.kairos.crawler.entities.CrawlJob;
import com.kairos.crawler.entities.CrawlStatus;
import com.kairos.crawler.models.UrlToCrawl;
import com.kairos.crawler.repositories.CrawlJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlJobScheduler {

    private final CrawlJobRepository crawlJobRepository;
    private final RabbitTemplate rabbitTemplate; // Spring's helper for sending messages

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void scheduleActiveJobs() {
        log.info("Checking for active crawl jobs to schedule...");
        List<CrawlJob> activeJobs = crawlJobRepository.findByStatus(CrawlStatus.ACTIVE);

        if (activeJobs.isEmpty()) {
            log.info("No active crawl jobs found.");
            return;
        }

        for (CrawlJob job : activeJobs) {
            log.info("Starting crawl job: '{}'", job.getName());
            job.setStatus(CrawlStatus.RUNNING);
            job.setLastRun(LocalDateTime.now());

            for (String seedUrl : job.getSeedUrls()) {
                UrlToCrawl message = new UrlToCrawl(seedUrl, 0); // Start at depth 0
                rabbitTemplate.convertAndSend(RabbitMQConfig.URL_QUEUE_NAME, message);
                log.debug("Published seed URL to queue: {}", seedUrl);
            }
            crawlJobRepository.save(job);
        }
    }
}