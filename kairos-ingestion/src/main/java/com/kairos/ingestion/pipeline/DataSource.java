package com.kairos.ingestion.pipeline;

import java.util.stream.Stream;

/**
 * Represents the source of data for an ingestion pipeline.
 * A DataSource produces a stream of raw data items of type T.
 *
 * @param <T> The type of the raw data items produced by this source (e.g., InputStream, File, String).
 */
@FunctionalInterface
public interface DataSource<T> {

    /**
     * Produces a stream of data items.
     * Using a Stream allows for lazy processing and handling large datasets efficiently.
     * @return A Stream of data items.
     */
	
    Stream<T> stream();
}