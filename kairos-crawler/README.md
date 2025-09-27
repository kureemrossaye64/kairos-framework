# KAIROS - Crawler

The `kairos-crawler` module is responsible for actively gathering content from various external sources to be processed and indexed by the Kairos Framework. It acts as the primary data acquisition component, feeding the `kairos-ingestion` pipeline with a steady stream of information from the web, APIs, and other sources.

## Key Features

- **Web Crawling:** Utilizes the **crawler4j** library to efficiently crawl websites, extract their content, and identify links for further exploration. This is ideal for building a knowledge base from public web pages.
- **YouTube Integration:** Integrates with the **Google YouTube Data API** to fetch video information, such as transcripts and metadata. This allows the framework to ingest content from video sources.
- **Asynchronous Processing:** Leverages **Spring AMQP (RabbitMQ)** to publish crawled content to a message queue. This decouples the crawling process from the ingestion process, creating a resilient and scalable architecture. The crawler can continue its work without waiting for the ingestion and embedding steps to be completed.
- **Data Persistence:** Uses Spring Data JPA to interact with the database, likely to keep track of crawled URLs, job status, and other metadata to avoid redundant work and manage the crawling process.

## Core Components

- **Crawlers:** The module contains specific crawler implementations for different data sources (e.g., a `WebCrawler` using crawler4j, a `YouTubeCrawler` using the Google API).
- **Message Publisher:** A component responsible for sending the raw content gathered by the crawlers to a RabbitMQ exchange. The `kairos-ingestion` module will be the consumer of these messages.
- **Configuration:** Contains the necessary configuration for connecting to external services like the YouTube API and for setting up the RabbitMQ connection.

## How It Works

A crawl job is initiated, either through a scheduled task, an API call, or a manual trigger. The appropriate crawler (e.g., web or YouTube) begins its work, fetching raw content (HTML, text, video metadata). Once a piece of content is successfully retrieved, the crawler passes it to a message publisher, which sends it to a dedicated RabbitMQ queue. From there, the `kairos-ingestion` module picks it up for parsing, chunking, and embedding.

## Dependencies

- **`kairos-core`:** For access to core data models and utilities.
- **crawler4j:** The core web crawling library.
- **google-api-services-youtube:** For interacting with the YouTube Data API.
- **Spring Boot AMQP:** For integrating with RabbitMQ.
- **Spring Data JPA:** For database interactions.

This module is essential for populating the Kairos Framework with rich, diverse content, which is the foundation for any knowledge-based AI application.