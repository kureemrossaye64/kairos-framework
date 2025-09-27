package com.kairos.crawler.entities;

import com.kairos.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawled_urls", indexes = {
    @Index(name = "idx_crawled_url_url_hash", columnList = "urlHash", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class CrawledUrl extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(nullable = false)
    private int urlHash; // Store a hash for efficient lookups

    private int httpStatusCode;

    @Column(columnDefinition = "TEXT")
    private String contentType;

    private String contentHash; // Hash of the content to detect changes

    private LocalDateTime lastCrawled;

    public CrawledUrl(String url) {
        this.url = url;
        this.urlHash = url.hashCode();
        this.lastCrawled = LocalDateTime.now();
    }
}