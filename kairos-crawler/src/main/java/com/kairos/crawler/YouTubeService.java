package com.kairos.crawler;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.kairos.crawler.models.DocumentExtraction;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class YouTubeService {

    private final YouTube youtube;
    private static final Pattern YOUTUBE_VIDEO_ID_PATTERN = Pattern.compile(
            "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*");

    public YouTubeService(
            @Value("${kairos.youtube.api-key}") String apiKey,
            @Value("${kairos.youtube.application-name}") String appName
    ) {
        try {
            this.youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null // No Http request initializer needed for public data
            )
            .setApplicationName(appName)
            .build();
            // Note: The API key is added to each request, not in the builder.
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to initialize YouTube service client", e);
            throw new RuntimeException("Could not initialize YouTube service", e);
        }
    }

    /**
     * Fetches details for a single YouTube video and formats it as a DocumentExtraction.
     * @param videoUrl The full URL of the YouTube video.
     * @return A DocumentExtraction object, or null if the video is not found or an error occurs.
     */
    public DocumentExtraction extractVideoDetails(String videoUrl) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            log.warn("Could not extract a valid video ID from URL: {}", videoUrl);
            return null;
        }

        try {
            YouTube.Videos.List request = youtube.videos()
                    .list("snippet,contentDetails");
            
            // Add the API key to the request
            request.setKey(System.getProperty("youtube.apikey"));
            
            VideoListResponse response = request.setId(videoId).execute();

            if (response.getItems().isEmpty()) {
                log.warn("No video found for ID: {}", videoId);
                return null;
            }

            Video video = response.getItems().get(0);
            String title = video.getSnippet().getTitle();
            String description = video.getSnippet().getDescription();
            
            // Combine title and description for a rich text content
            String content = "Title: " + title + "\n\n" + "Description:\n" + description;

            // Create metadata map
            Map<String, List<String>> metadata = new HashMap<>();
            metadata.put("title", List.of(title));
            metadata.put("source_url", List.of(videoUrl));
            metadata.put("media_type", List.of("video"));
            metadata.put("platform", List.of("YouTube"));
            metadata.put("channel_title", List.of(video.getSnippet().getChannelTitle()));
            metadata.put("published_at", List.of(video.getSnippet().getPublishedAt().toString()));
            
            // TODO: In a more advanced version, we would also fetch the transcript here if available.
            // The YouTube Transcript API is a separate, unofficial library but very powerful.

            log.info("Successfully extracted details for YouTube video: '{}'", title);
            return new DocumentExtraction(videoUrl, metadata, content);

        } catch (IOException e) {
            log.error("Failed to fetch video details from YouTube API for ID: {}", videoId, e);
            return null;
        }
    }

    /**
     * A robust regex-based method to extract the video ID from any common YouTube URL format.
     */
    private String extractVideoId(String url) {
        Matcher matcher = YOUTUBE_VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}