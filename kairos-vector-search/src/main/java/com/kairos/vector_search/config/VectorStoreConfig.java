package com.kairos.vector_search.config;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

/**
 * Configures the connection to the ChromaDB vector store. This configuration is
 * activated only when "kairos.vector-store.chroma.base-url" is set.
 */
@Configuration
@ConditionalOnProperty(name = "kairos.vector-store.pgvector.host")
@EnableConfigurationProperties(PgVectorProperties.class)
@Slf4j
public class VectorStoreConfig {
	
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper m = new ObjectMapper();
		m.registerModule(new Jdk8Module());
		m.registerModule(new JavaTimeModule());
		m.registerModule(new JtsModule());
		return m;
	}
	
	 

		@Bean
		public EmbeddingStore<TextSegment> pgVectorEmbeddingStore(PgVectorProperties props) {
	        log.info("Creating PgVectorEmbeddingStore for table '{}' with dimension {}", props.getTableName(), props.getDimension());
			// This bean provides the high-level LangChain4j integration for simple vector operations.
	        // It can automatically create the table if it doesn't exist.
	        return PgVectorEmbeddingStore.builder()
	        		.host(props.getHost())
	        		.port(props.getPort())
	        		.createTable(true)
	        		.database(props.getDatabase())
	        		.dimension(props.getDimension())
	        		.password(props.getPassword())
	        		.user(props.getUsername())
	                .table(props.getTableName())
	                .dimension(props.getDimension())
	                .useIndex(true)
	                .indexListSize(384)
	                .dropTableFirst(true)
	                .build();
		}

	/*
	 * @Bean public OpenSearchClient openSearchClient(OpenSearchProperties props) {
	 * String serverUrl = props.getBaseUrl(); String apiKey = props.getApiKey();
	 * String userName = props.getUsername(); String password = props.getPassword();
	 * HttpHost openSearchHost; try { openSearchHost = HttpHost.create(serverUrl); }
	 * catch (URISyntaxException se) { log.error("[I/O OpenSearch Exception]", se);
	 * throw new OpenSearchRequestFailedException(se.getMessage()); }
	 * 
	 * JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper());
	 * 
	 * OpenSearchTransport transport =
	 * ApacheHttpClient5TransportBuilder.builder(openSearchHost)
	 * .setMapper(mapper).setHttpClientConfigCallback(httpClientBuilder -> {
	 * 
	 * if (!isNullOrBlank(apiKey)) { httpClientBuilder
	 * .setDefaultHeaders(singletonList(new BasicHeader("Authorization", "ApiKey " +
	 * apiKey))); }
	 * 
	 * if (!isNullOrBlank(userName) && !isNullOrBlank(password)) {
	 * BasicCredentialsProvider credentialsProvider = new
	 * BasicCredentialsProvider(); credentialsProvider.setCredentials(new
	 * AuthScope(openSearchHost), new UsernamePasswordCredentials(userName,
	 * password.toCharArray()));
	 * httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider); }
	 * 
	 * httpClientBuilder.setConnectionManager(
	 * PoolingAsyncClientConnectionManagerBuilder.create().build());
	 * 
	 * return httpClientBuilder; }).build();
	 * 
	 * OpenSearchClient client = new OpenSearchClient(transport);
	 * 
	 * return client; }
	 * 
	 * @Bean public EmbeddingStore<TextSegment>
	 * opensearchEmbeddingStore(OpenSearchProperties props) {
	 * EmbeddingStore<TextSegment> embeddingStore =
	 * OpenSearch3EmbeddingStore.builder().serverUrl(props.getBaseUrl())
	 * .indexName(props.getIndexName()) .apiKey(props.getApiKey()) // Optional: for
	 * secured OpenSearch instances .userName(props.getUsername()) // Optional: for
	 * secured OpenSearch instances .password(props.getPassword()) // Optional: for
	 * secured OpenSearch instances
	 * 
	 * .build();
	 * 
	 * return embeddingStore; }
	 */
}