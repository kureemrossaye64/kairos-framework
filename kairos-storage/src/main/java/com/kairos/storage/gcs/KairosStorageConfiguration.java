package com.kairos.storage.gcs;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
@ConditionalOnProperty(name = "kairos.storage.gcs.bucket-name")
@EnableConfigurationProperties(GcsProperties.class)
@ComponentScan(basePackages = "com.kairos.storage.gcs")
public class KairosStorageConfiguration {
	
	
	public GoogleCredentials getGoogleCredential() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("foodguard-453108-5d360d30dd86.json"));
		return credentials;
	}
	
	@Bean
	public Storage getStorage(GcsProperties prop) throws IOException {
		
			return StorageOptions.newBuilder().setCredentials(getGoogleCredential()).setProjectId(prop.getProjectId())
					.build().getService();
	}
}