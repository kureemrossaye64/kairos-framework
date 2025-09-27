package com.kairos.vector_search.model;

import lombok.Builder;
import lombok.Value;
import java.util.Map;
import java.util.UUID;

/**
 * A generic, domain-agnostic representation of a document to be indexed in the vector store.
 * This class is an immutable value object.
 *
 * @param id A unique identifier for this document.
 * @param content The actual text content to be embedded and indexed.
 * @param metadata A flexible map for storing any additional, filterable data
 *                 (e.g., source file name, associated entity ID, creation date).
 */
@Value
@Builder
public class VdbDocument {
    UUID id;
    String content;
    Map<String, Object> metadata;
}