package com.kairos.storage.gcs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StorageFile {
	
	private String storageUri;
	
	private String contentType;
	
	private String name;

}
