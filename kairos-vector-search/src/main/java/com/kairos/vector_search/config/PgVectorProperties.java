package com.kairos.vector_search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties(prefix = "kairos.vector-store.pgvector")
@Data
public class PgVectorProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String tableName = "embeddings"; // Default table name
    private Integer dimension=384; // Embedding dimension, must be configured
    private String database;
}