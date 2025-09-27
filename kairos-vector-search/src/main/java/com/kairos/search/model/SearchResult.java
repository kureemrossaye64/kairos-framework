package com.kairos.search.model;

import java.util.List;
import java.util.UUID;

/**
 * A DTO to encapsulate the results of a search, primarily returning the IDs of matching documents.
 */
public record SearchResult(
    long totalHits,
    List<UUID> documentIds
) {}