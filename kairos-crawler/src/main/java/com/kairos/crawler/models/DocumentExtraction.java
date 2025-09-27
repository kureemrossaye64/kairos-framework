package com.kairos.crawler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class DocumentExtraction {
    private final String reference; // The URL of the document
    private final Map<String, List<String>> metadata;
    private final String content;
}