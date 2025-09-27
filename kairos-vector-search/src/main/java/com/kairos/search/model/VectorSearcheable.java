package com.kairos.search.model;

import java.util.UUID;

public interface VectorSearcheable {
	
	public UUID getId();
	
	public	float[] getTextEmbedding();
	
	public void setTextEmbedding(float[] embeddings);

}
