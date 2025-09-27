package com.kairos.vector_search.service.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.ai_abstraction.service.EmbeddingModel;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.Searchable;
import com.kairos.search.annotation.SearchableField;
import com.kairos.search.model.SearchQuery;
import com.kairos.search.model.SearchResult;
import com.kairos.search.model.VectorSearcheable;
import com.kairos.search.query_builder.PgVectorQueryBuilder;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;
import com.pgvector.PGvector;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class PgVectorStoreServiceImpl implements VectorStoreService {

    // For simple, high-level vector operations
    private final EmbeddingStore<TextSegment> embeddingStore;
    
    // For generating embeddings
    private final EmbeddingModel embeddingModel;
    
    // For direct, complex SQL queries
    private final JdbcTemplate jdbcTemplate;
    
    // For building SQL queries
    private final PgVectorQueryBuilder queryBuilder;
    
    private final ObjectMapper mapper;

    // --- LangChain4j Delegated Methods ---

    @Override
    public void addDocument(VdbDocument document) {
        log.info("Adding document with id: {} to pgvector.", document.getId());
        TextSegment segment = TextSegment.from(document.getContent(), toLangChainMetadata(document));
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    @Override
    public void addDocuments(List<VdbDocument> documents) {
        log.info("Adding {} documents in batch to pgvector.", documents.size());
        List<TextSegment> segments = documents.stream()
                .map(doc -> TextSegment.from(doc.getContent(), toLangChainMetadata(doc)))
                .collect(Collectors.toList());

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
    }

    @Override
    public List<VdbDocument> findRelevant(String queryText, int topK) {
        log.debug("Finding top {} relevant documents for query: '{}'", topK, queryText);
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .filter(new IsEqualTo("entity", "vsearch"))
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> VdbDocument.builder()
                        .id(UUID.fromString(match.embeddingId()))
                        .content(match.embedded().text())
                        .metadata(match.embedded().metadata().toMap())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Direct JDBC Methods for Hybrid Search & Indexing ---

    @Override
    public void index(UUID documentId, Object document) {
    	 Class<?> entityClass = document.getClass();
         Searchable searchable = entityClass.getAnnotation(Searchable.class);
         if (searchable == null) {
             log.warn("Attempted to index an object without @Searchable annotation: {}", entityClass.getSimpleName());
             return;
         }

         if (!(document instanceof VectorSearcheable)) {
              throw new IllegalArgumentException("Document to be indexed must implement VectorSearcheable.");
         }
         
         addEmbedding((VectorSearcheable)document);

         String tableName = searchable.indexName();
         String geoFieldName = findAnnotatedFieldName(entityClass, GeoPointField.class);

         try {
             // Use reflection to get values from the document instance
             UUID id = ((VectorSearcheable)document).getId();
             float[] embedding = ((VectorSearcheable) document).getTextEmbedding();
             Point coordinates = (Point) getFieldValue(document, geoFieldName);

             if (id == null || embedding == null || coordinates == null) {
                 log.error("Cannot index document with null id, embedding, or coordinates. ID: {}", id);
                 return;
             }

             String sql = String.format(
                 "INSERT INTO %s (id, text_embedding, %s) VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) " +
                 "ON CONFLICT (id) DO UPDATE SET " +
                 "text_embedding = EXCLUDED.text_embedding, " +
                 "%s = EXCLUDED.%s",
                 tableName, geoFieldName, geoFieldName, geoFieldName
             );

             jdbcTemplate.update(sql,
                 id,
                 new PGvector(embedding),
                 coordinates.getX(), // longitude
                 coordinates.getY()
             );

             log.info("Successfully indexed document with ID: {}", id);

         } catch (Exception e) {
             log.error("Failed to index document of type {}", entityClass.getSimpleName(), e);
             throw new RuntimeException("Indexing failed", e);
         }
    }

    /**
     * Creates a table in PostgreSQL with appropriate columns and indexes for searching.
     * This replaces the OpenSearch `createIndexWithMapping`.
     */
    public void createIndexWithMapping(Class<?> entityClass) {
    	Searchable searchable = entityClass.getAnnotation(Searchable.class);
        if (searchable == null) {
            throw new IllegalArgumentException("Entity class is not @Searchable: " + entityClass.getName());
        }
        String tableName = searchable.indexName();
        String geoFieldName = findAnnotatedFieldName(entityClass, GeoPointField.class);
        int dimension = embeddingModel.dimension();

        log.info("Generating DDL for search index table '{}' from entity {}", tableName, entityClass.getSimpleName());

        // 1. DDL for creating the table
        String createTableSql = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id UUID PRIMARY KEY, " +
            "text_embedding VECTOR(%d), " +
            "%s GEOGRAPHY(Point, 4326))",
            tableName, dimension, geoFieldName
        );

        // 2. DDL for the HNSW index for efficient vector search
        String createHnswIndexSql = String.format(
            "CREATE INDEX IF NOT EXISTS %s_embedding_hnsw_idx ON %s USING hnsw (text_embedding vector_l2_ops)",
            tableName, tableName
        );

        // 3. DDL for the GIST index for efficient geospatial search
        String createGistIndexSql = String.format(
            "CREATE INDEX IF NOT EXISTS %s_coords_gist_idx ON %s USING GIST (%s)",
            tableName, tableName, geoFieldName
        );

        log.info("Executing DDL for table '{}'", tableName);
        jdbcTemplate.execute(createTableSql);
        jdbcTemplate.execute(createHnswIndexSql);
        jdbcTemplate.execute(createGistIndexSql);
        log.info("Table and indexes for '{}' are ready.", tableName);
    }
    
    @Override
    public SearchResult search(SearchQuery query, Class<?> entityClass) {
    	try {
            int resultLimit = 20; // Default limit
            PgVectorQueryBuilder.PreparedQuery preparedQuery = queryBuilder.buildSearchQuery(query, entityClass, embeddingModel, resultLimit);
            
            List<UUID> ids = jdbcTemplate.queryForList(
                preparedQuery.getSql(),
                UUID.class,
                preparedQuery.getParams()
            );
            
            return new SearchResult(ids.size(), ids);
        } catch (Exception e) {
            log.error("Failed during PostgreSQL hybrid search", e);
            throw new RuntimeException("Search failed", e);
        }
    }
    
    public SearchResult vectorSearch(SearchQuery query, Class<?> entityClass, int neighbours) {
        try {
            PgVectorQueryBuilder.PreparedQuery preparedQuery = queryBuilder.buildSearchQuery(query, entityClass, embeddingModel, neighbours);
            List<UUID> ids = jdbcTemplate.query(
                preparedQuery.getSql(),
                (rs, rowNum) -> UUID.fromString(rs.getString("id")),
                preparedQuery.getParams()
            );
            return new SearchResult(ids.size(), ids);
        } catch (Exception e) {
            throw new RuntimeException("Failed during PostgreSQL vector search", e);
        }
    }

    @Override
    public void delete(Class<?> entityClass, UUID documentId) {
    	String indexName = getTableName(entityClass);
    	log.info("Deleting document {} from table '{}'", documentId, indexName);
        String sql = String.format("DELETE FROM %s WHERE id = ?", indexName);
        jdbcTemplate.update(sql, documentId);
    }

    @Override
    public void deleteAll(Class<?> entityClass, List<UUID> documentIds) {
    	String indexName = getTableName(entityClass);
        log.info("Deleting {} documents from table '{}'", documentIds.size(), indexName);
        String idsPlaceholder = documentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("DELETE FROM %s WHERE id IN (%s)", indexName, idsPlaceholder);
        jdbcTemplate.update(sql, documentIds.toArray());
    }

    // --- Helper Methods ---

    private void addEmbedding(VectorSearcheable vs) {
        String textContent = getTextContent(vs);
        if (textContent != null && !textContent.isBlank()) {
            float[] embedding = embeddingModel.embed(textContent).content().vector();
            vs.setTextEmbedding(embedding);
        }
    }

    private String getTextContent(Object document) {
        if (!document.getClass().isAnnotationPresent(Searchable.class)) {
            try {
                return mapper.writeValueAsString(document);
            } catch (JsonProcessingException e) {
                log.error("Error serializing non-searchable object to string for embedding", e);
                return "";
            }
        }

        try {
            Map<String, Object> vals = mapper.convertValue(document, new TypeReference<>() {});
            StringBuilder b = new StringBuilder();
            for (Field field : document.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(SearchableField.class)) {
                    Object value = vals.get(field.getName());
                    if (value != null) {
                        b.append(value).append(" ");
                    }
                }
            }
            return b.toString().trim();
        } catch (Exception e) {
            log.error("Error reflecting fields to build text content for embedding", e);
            return "";
        }
    }
    
    private Metadata toLangChainMetadata(VdbDocument document) {
        Metadata metadata = new Metadata();
        document.getMetadata().forEach((key, value) -> metadata.put(key, value.toString()));
        metadata.put("internal_id", document.getId().toString());
        metadata.put("entity", "vsearch");
        return metadata;
    }

    @SuppressWarnings("unchecked")
	private String findAnnotatedFieldName(Class<?> clazz, @SuppressWarnings("rawtypes") Class annotation) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing @" + annotation.getSimpleName() + " annotation on class " + clazz.getName()));
    }

    private Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    private String getTableName(Class<?> entityClass) {
        Searchable searchableAnnotation = entityClass.getAnnotation(Searchable.class);
        if (searchableAnnotation == null || searchableAnnotation.indexName().isBlank()) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Searchable or indexName is empty.");
        }
        return searchableAnnotation.indexName();
    }
}