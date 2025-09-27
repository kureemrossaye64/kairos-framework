# KAIROS - Vector & Search

The `kairos-vector-search` module is a sophisticated component of the Kairos Framework responsible for indexing, storing, and querying vector embeddings and their associated metadata. This module provides the core functionality for semantic search, enabling AI agents to find relevant information from a large knowledge base based on meaning rather than just keywords.

## Key Features

- **Vector Database Integration:** Provides a robust integration with **PostgreSQL** using the **pgvector** extension. This allows for efficient storage and similarity searching of high-dimensional vector embeddings directly within the database.
- **LangChain4j Integration:** Leverages the **LangChain4j** library for seamless integration with vector stores. It uses LangChain4j's `EmbeddingStore` abstraction and provides a concrete implementation based on `PgVectorEmbeddingStore`, which handles the complexities of interacting with the pgvector extension.
- **Combined Storage:** This module is designed to store both the vector embeddings and the original text content (or metadata) together. This is crucial for retrieval-augmented generation (RAG), as it allows the system to retrieve the relevant text chunks that correspond to the vector search results.
- **Flexible Indexing:** Offers a flexible service layer that allows for the indexing of documents with rich metadata. This enables more advanced search scenarios, such as filtering by source, date, or other attributes.
- **Search Abstraction:** Defines a `VectorSearchService` that abstracts the details of the querying process. This allows other modules, like `kairos-agentic-framework`, to perform semantic searches without needing to know the specifics of the underlying vector database implementation.

## Core Components

- **`VectorSearchService`:** The primary service for interacting with the vector database. It provides methods for indexing new documents (`index(document)`) and searching for relevant documents based on a query embedding (`search(queryEmbedding, topK)`).
- **`PgVectorEmbeddingStore`:** The core component from LangChain4j that is configured and used to manage the lifecycle of embeddings within the PostgreSQL database.
- **Entity Definitions:** Contains the JPA entity definitions for the tables that store the embeddings and their associated metadata. This defines the schema for the vector store.
- **Configuration:** Provides the necessary Spring configuration to set up the `DataSource` and the `PgVectorEmbeddingStore`, connecting it to the PostgreSQL database and configuring parameters like the vector dimensions.

## How It Works

1.  **Indexing:** The `kairos-ingestion` module processes a document, splits it into chunks, and generates a vector embedding for each chunk. It then calls the `index` method of the `VectorSearchService` in this module, passing the text chunk, its embedding, and any metadata. The service then persists this information in the pgvector-enabled database.
2.  **Searching:** An AI agent in the `kairos-agentic-framework` needs to answer a user's question. It first generates an embedding for the user's query. It then calls the `search` method of the `VectorSearchService`, providing the query embedding. The service uses pgvector's similarity search capabilities (e.g., cosine similarity) to find the most relevant document chunks from the database and returns them to the agent. The agent then uses this retrieved information to formulate its response.

## Dependencies

- **`kairos-core`:** For core utilities and base entity classes.
- **`kairos-ai-abstraction`:** To access the embedding models when generating embeddings for queries.
- **`langchain4j-pgvector`:** The key LangChain4j integration library for pgvector.
- **`pgvector` (JDBC wrapper):** The JDBC driver that adds support for the `vector` data type.
- **PostgreSQL Driver:** The standard JDBC driver for PostgreSQL.
- **Spring Boot:** For Data JPA, JDBC, and overall application management.

This module is the heart of the framework's knowledge retrieval capabilities, transforming a simple database into a powerful engine for semantic understanding and search.