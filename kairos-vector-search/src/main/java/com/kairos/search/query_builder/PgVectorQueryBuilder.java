// src/main/java/com/kairos/search/query_builder/PgVectorQueryBuilder.java
package com.kairos.search.query_builder;

import com.kairos.ai_abstraction.service.EmbeddingModel;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.Searchable;
import com.kairos.search.model.SearchQuery;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PgVectorQueryBuilder {

    private final Map<Class<?>, String> geoFieldCache = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    public static class PreparedQuery {
        private final String sql;
        private final Object[] params;
    }

    /**
     * Builds a hybrid vector and geospatial search query.
     *
     * @return A PreparedQuery containing the SQL and its parameters.
     */
    public PreparedQuery buildSearchQuery(SearchQuery searchQuery, Class<?> entityClass, EmbeddingModel embeddingModel, int limit) {
        if (searchQuery.getTextQuery() == null && searchQuery.getLocation() == null) {
            throw new IllegalArgumentException("Search query must contain at least a text query or a location.");
        }

        String tableName = getTableName(entityClass);
        Optional<String> geoFieldNameOpt = getGeoField(entityClass);

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id FROM ").append(tableName);
        StringBuilder whereClause = new StringBuilder();

        // 1. Build Geospatial Filter (WHERE clause)
        if (searchQuery.getLocation() != null && searchQuery.getRadiusInKm() != null) {
            String geoFieldName = geoFieldNameOpt.orElseThrow(() ->
                    new IllegalArgumentException("Geospatial search requires a @GeoPointField on " + entityClass.getName()));

            // ST_DWithin is efficient with a GIST index. We use 'geography' type for meter-based distance.
            whereClause.append("ST_DWithin(").append(geoFieldName).append(", ST_MakePoint(?, ?)::geography, ?)");
            params.add(searchQuery.getLocation().getX()); // Longitude
            params.add(searchQuery.getLocation().getY()); // Latitude
            params.add(searchQuery.getRadiusInKm() * 1000); // Convert km to meters
        }

        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        // 2. Build Vector Similarity Ranking (ORDER BY clause)
        if (StringUtils.hasText(searchQuery.getTextQuery())) {
            // Embed the query text to get the search vector
            float[] queryVector = embeddingModel.embed(searchQuery.getTextQuery()).content().vector();
            // The <-> operator from pgvector calculates L2 distance.
            sql.append(" ORDER BY text_embedding <-> ?");
            params.add(new PGvector(queryVector));
        }

        sql.append(" LIMIT ?");
        params.add(limit);

        log.debug("Generated SQL: {}", sql);
        return new PreparedQuery(sql.toString(), params.toArray());
    }

    private String getTableName(Class<?> entityClass) {
        Searchable searchableAnnotation = entityClass.getAnnotation(Searchable.class);
        if (searchableAnnotation == null || searchableAnnotation.indexName().isBlank()) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Searchable or indexName is empty.");
        }
        return searchableAnnotation.indexName();
    }

    private Optional<String> getGeoField(Class<?> entityClass) {
        return Optional.ofNullable(geoFieldCache.computeIfAbsent(entityClass, clazz -> {
            log.debug("Reflecting @GeoPointField annotation for class: {}", clazz.getSimpleName());
            return Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(GeoPointField.class))
                    .map(Field::getName)
                    .findFirst()
                    .orElse(null);
        }));
    }
}