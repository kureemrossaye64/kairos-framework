package com.kairos.ai_abstraction.service;

import java.util.concurrent.CompletableFuture;

public interface ImageAnalysisService {
	
	
	public CompletableFuture<String> analyze(String storageURI);

}
