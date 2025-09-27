# KAIROS - Storage Service

The `kairos-storage` module is responsible for abstracting interactions with cloud-based object storage providers. Its primary purpose is to provide a simple and consistent interface for uploading, downloading, and managing files, decoupling the rest of the Kairos Framework from the specifics of the underlying storage technology.

## Key Features

- **Storage Abstraction:** Defines a clean `StorageService` interface that hides the implementation details of the storage provider. This allows other modules, such as `kairos-ingestion`, to store and retrieve files without being coupled to a specific vendor like Google Cloud Storage or Amazon S3.
- **Provider-Specific Implementation:** Includes a concrete implementation for **Google Cloud Storage (GCS)**, leveraging the `google-cloud-storage` SDK to handle the actual communication with the GCS API.
- **Extensibility:** The architecture makes it straightforward to add support for other storage providers in the future. To add support for AWS S3, for example, one would simply need to create a new `S3StorageServiceImpl` that implements the `StorageService` interface.
- **Centralized Configuration:** The configuration required to connect to the storage provider (e.g., bucket names, credentials) is managed within this module, providing a single point of control for storage settings.

## Core Components

- **`StorageService`:** The main interface that defines the core file operations, such as `save(file)`, `load(fileName)`, and `delete(fileName)`.
- **`GoogleCloudStorageServiceImpl`:** The concrete implementation of `StorageService` that uses the Google Cloud Storage client library to perform file operations on GCS.
- **Configuration Beans:** Spring configuration classes that are responsible for instantiating the `Storage` client from the Google Cloud SDK, using credentials and project information provided in the application's properties.

## How It Works

When a module like `kairos-ingestion` receives a file to be processed, it first calls the `save` method of the `StorageService`. The `StorageService` implementation then takes care of uploading the file to the configured object storage bucket (e.g., in GCS) and returns a unique identifier or path for the stored file. This identifier can then be used to retrieve the file later if needed.

## Dependencies

- **`kairos-core`:** For any shared utilities or configuration that might be required.
- **`google-cloud-storage`:** The Google Cloud SDK for Java, used to interact with Google Cloud Storage.
- **Spring Boot:** For dependency injection and configuration management.
- **Lombok:** To reduce boilerplate code.

By abstracting away the details of file storage, the `kairos-storage` module helps to create a more portable and maintainable system, allowing the framework to adapt to different cloud environments with minimal code changes.