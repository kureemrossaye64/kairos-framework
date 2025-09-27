package com.kairos.crawler.models;

import java.io.Serializable;

// A record is perfect for an immutable message DTO.
public record UrlToCrawl(
    String url,
    int depth // Current crawl depth
) implements Serializable {}