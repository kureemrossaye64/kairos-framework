package com.kairos.storage.gcs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kairos.storage.gcs")
@Validated
@Getter 
@Setter
public class GcsProperties {
    @NotBlank
    private String bucketName;
    
    private String projectId;
}