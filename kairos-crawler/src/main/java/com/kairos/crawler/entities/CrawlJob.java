package com.kairos.crawler.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.kairos.core.entity.BaseEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crawl_jobs")
@Getter
@Setter
@NoArgsConstructor
public class CrawlJob extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CrawlStatus status = CrawlStatus.PAUSED;

	@ElementCollection
    @CollectionTable(name = "seed_urls") // Optional: Customizes the table name for the collection
    @Column(name = "seed_url")
	private List<String> seedUrls;

	private int maxDepth = 5; // How many links deep to follow from the seed

	private LocalDateTime lastRun;
}

