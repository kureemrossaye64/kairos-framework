package com.kairos.core.ingestion;

/**
 * An interface defining the contract for an ingestion router.
 * The router is responsible for taking an IngestionRequest and forwarding it
 * to the appropriate processing pipeline or component.
 */
public interface IIngestionRouter {
	
	/**
     * Adds a new ingestion request to the routing queue or process.
     * This method is the entry point for all data that needs to be ingested
     * into the system.
     *
     * @param request The ingestion request containing the data and metadata.
     */
	public void addRequest(IngestionRequest request);

}
