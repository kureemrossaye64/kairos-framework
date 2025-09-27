# KAIROS - Ingestion Framework

The `kairos-ingestion` module provides a robust and extensible framework for building data ingestion pipelines. Its primary role is to take raw content from various sources—such as documents uploaded via an API or messages from the `kairos-crawler`—and process it into a structured format suitable for storage and vector-based searching.

## Key Features

- **Document Parsing:** Leverages **Apache Tika** to parse a wide variety of document formats, including PDF, Microsoft Office documents (DOCX, PPTX), and more. This allows the framework to extract clean, textual content from complex files.
- **Content Processing Pipeline:** Implements a flexible pipeline for processing raw data. This typically includes steps such as:
    1.  **Parsing:** Extracting text from the source file.
    2.  **Chunking:** Splitting the extracted text into smaller, semantically meaningful segments.
    3.  **Embedding:** Converting the text chunks into vector embeddings using a model from the `kairos-ai-abstraction` module.
    4.  **Indexing:** Storing the embeddings and their associated metadata in a vector database via the `kairos-vector-search` module.
- **Asynchronous Operations:** Designed to handle ingestion tasks asynchronously. It can consume messages from a RabbitMQ queue (populated by the `kairos-crawler`) or handle file uploads from a web endpoint, processing them in the background without blocking the user.
- **Integration with Core Services:** Tightly integrated with other Kairos modules to perform its functions. It relies on `kairos-storage` to handle raw file persistence, `kairos-ai-abstraction` to generate embeddings, and `kairos-vector-search` to index the final data.

## Core Components

- **Ingestion Service:** The main service that orchestrates the ingestion pipeline. It takes a source (e.g., a `MultipartFile` or a message payload) and manages its journey from raw data to indexed vector embeddings.
- **Document Parsers:** A set of components, primarily using Apache Tika, that are responsible for extracting text and metadata from different file types.
- **Text Splitters/Chunkers:** Logic for dividing long documents into smaller chunks. This is a critical step for effective vector-based retrieval, as it ensures that the embeddings represent focused, semantically coherent pieces of information.

## How It Works

The ingestion process can be triggered in several ways, such as a user uploading a file through a REST API endpoint or a message arriving from the `kairos-crawler`.

1.  The raw file or content is received by the `IngestionService`.
2.  If it's a file, it is first saved to a persistent storage location using the `kairos-storage` module.
3.  The file's content is extracted using the appropriate Apache Tika parser.
4.  The extracted text is split into smaller chunks.
5.  Each chunk is sent to the `kairos-ai-abstraction` module to be converted into a vector embedding.
6.  The text chunk, its embedding, and any relevant metadata are then passed to the `kairos-vector-search` module to be indexed in the vector database.

## Dependencies

- **`kairos-core`:** For core entities and utilities.
- **`kairos-vector-search`:** To index the generated embeddings and metadata.
- **`kairos-storage`:** To store the original raw files.
- **`kairos-ai-abstraction`:** To access the embedding models needed to create vector representations of the text.
- **Apache Tika:** The primary library for parsing document content.
- **Spring Boot:** For building the application and managing asynchronous tasks.

This module acts as the bridge between raw data and searchable knowledge, making it a cornerstone of the Kairos Framework's ability to build intelligent, context-aware AI applications.