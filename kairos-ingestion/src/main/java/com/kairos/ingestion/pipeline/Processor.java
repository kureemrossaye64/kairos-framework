package com.kairos.ingestion.pipeline;

import java.util.stream.Stream;

/**
 * Represents a processing step in the ingestion pipeline.
 * A Processor transforms a stream of input data (I) into a stream of output data (O).
 * This can be a one-to-one, one-to-many, or many-to-one transformation.
 *
 * @param <I> The type of the input data items.
 * @param <O> The type of the output data items.
 */
@FunctionalInterface
public interface Processor<I, O> {

    /**
     * Processes an input stream and returns an output stream.
     * @param inputStream The stream of data to be processed.
     * @return A Stream of processed data.
     */
    Stream<O> process(Stream<I> inputStream);
}