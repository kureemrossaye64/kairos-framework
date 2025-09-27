package com.kairos.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity as a document that should be automatically synchronized
 * with the search engine (e.g., OpenSearch).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    /**
     * The name of the search index where documents of this entity type will be stored.
     * For example, "facilities" or "partners".
     * @return The name of the index.
     */
    String indexName();
}