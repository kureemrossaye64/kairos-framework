package com.kairos.search.model;

import lombok.Builder;
import lombok.Data;

import org.locationtech.jts.geom.Point;
import java.util.Map;

/**
 * A builder-style DTO for constructing complex, multi-faceted search queries.
 */
@Builder
@Data
public class SearchQuery {
	private String textQuery;
    private Point location;
    private Double radiusInKm;
    private Map<String, String> keywordFilters;
}