package com.kairos.crawler.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.crawler.entities.CrawlJob;
import com.kairos.crawler.entities.CrawlStatus;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, UUID> {

	List<CrawlJob> findByStatus(CrawlStatus active);

}
