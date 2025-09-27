package com.kairos.ingestion.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Stream;

/**
 * A builder and runner for the ingestion pipeline.
 * It chains together a DataSource, zero or more Processors, and a Sink.
 * The pipeline is executed lazily when the 'run' method is called.
 *
 * @param <T> The initial type of data from the DataSource.
 */
@Slf4j
@RequiredArgsConstructor
public class Pipeline<T> {

    private final DataSource<T> source;
    private final Stream<T> currentStream;

    /**
     * Factory method to start building a pipeline with a given data source.
     * @param source The DataSource to start with.
     * @return A new Pipeline instance.
     * @param <S> The type of data from the source.
     */
    public static <S> Pipeline<S> from(DataSource<S> source) {
        return new Pipeline<>(source, source.stream());
    }

    /**
     * Adds a processing step to the pipeline.
     * @param processor The Processor to apply.
     * @return A new Pipeline instance with the processor added.
     * @param <O> The output type of the processor.
     */
    public <O> Pipeline<O> through(Processor<T, O> processor) {
        return new Pipeline(this.source, processor.process(this.currentStream));
    }

    /**
     * Sets the final destination (Sink) for the pipeline and executes it.
     * This is the terminal operation that triggers all preceding lazy operations.
     */
    public void to(Sink<T> sink) {
        log.info("Starting ingestion pipeline execution...");
        long startTime = System.currentTimeMillis();
        try {
            sink.consume(this.currentStream);
            long endTime = System.currentTimeMillis();
            log.info("Pipeline execution finished successfully in {} ms.", (endTime - startTime));
        } catch (Exception e) {
            log.error("Pipeline execution failed.", e);
            // Depending on requirements, you might want to re-throw or handle differently
            throw new RuntimeException("Pipeline failed", e);
        } finally {
            // Streams should be closed to release resources
            this.currentStream.close();
        }
    }
}