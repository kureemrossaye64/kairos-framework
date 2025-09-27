package com.kairos.ai_abstraction.service;

import java.util.concurrent.CompletableFuture;

import dev.langchain4j.model.output.structured.Description;

public interface VideoAnalysisService {
	
	public CompletableFuture<VideoAnalysisResult> analyze(String storageURI);
	
	
	@Description("The language in the video may be Mauritian Creole or Arabic or French or English")
	public record VideoAnalysisResult(
			@Description("the audio transcipt in the video if there is any audio") String transcript,

			@Description("A narrative of the video") String visualNarrative

	) {

	}

}
