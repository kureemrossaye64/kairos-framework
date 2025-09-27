package com.kairos.ingestion.pipeline;

import java.util.stream.Stream;

/**
 * Represents the final destination for data in an ingestion pipeline.
 * A Sink consumes a stream of processed data items of type T and performs a terminal action,
 * such as saving to a database or writing to a file.
 *
 * @param <T> The type of the data items consumed by this sink.
 */
@FunctionalInterface
public interface Sink<T> {

    /**
     * Consumes the given stream of data.
     * This is a terminal operation that triggers the execution of the entire pipeline.
     * @param stream The stream of data to be consumed.
     */
    void consume(Stream<T> stream);
}