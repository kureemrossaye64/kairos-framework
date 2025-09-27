package com.kairos.crawler.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.crawler.entities.CrawledUrl;

public interface CrawledUrlRepository extends JpaRepository<CrawledUrl, UUID> {
    boolean existsByUrlHash(int urlHash);
}