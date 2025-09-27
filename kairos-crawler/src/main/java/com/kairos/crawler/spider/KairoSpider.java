package com.kairos.crawler.spider;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;

import com.kairos.crawler.models.DocumentExtraction;

import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KairoSpider extends WebCrawler implements WebCrawlerFactory<KairoSpider>{

    // A pattern to filter out non-document/media file extensions.
    private static final Pattern FILTERS = Pattern.compile(
        ".*(\\.(css|js|gif|jpg|jpeg|png|mp3|mp4|zip|gz))$");

    // This is where we will store the results of the crawl.
    @Getter
    private final List<DocumentExtraction> extractions = new ArrayList<>();
    private final String startDomain;

    public KairoSpider(String startUrl) {
        this.startDomain = getDomainName(startUrl);
    }

    /**
     * This method decides if a URL discovered on a page should be added to the crawl queue.
     * We use it to stay on the same domain and filter out unwanted file types.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // 1. Don't visit already filtered extensions.
        // 2. Only visit URLs from the same starting domain to prevent crawling the whole internet.
        return !FILTERS.matcher(href).matches() && getDomainName(href).equals(startDomain);
    }

    /**
     * This function is called when a page is successfully downloaded and parsed.
     * This is where we extract the content.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        log.debug("Visiting page: {}", url);

        // We are only interested in successful fetches with parsable data.
        if (page.getStatusCode() == HttpStatus.SC_OK && page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            
            // Extract the title from the HTML metadata.
            String title = htmlParseData.getTitle();
            // Get all outgoing links to be queued by the controller.
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            // Get the clean, extracted text content of the page body.
            String textContent = htmlParseData.getText();
            
            // Create a simplified metadata map.
            HashMap<String, List<String>> metadata = new HashMap<>();
            metadata.put("title", List.of(title != null ? title : "Untitled"));
            metadata.put("source_url", List.of(url));
            metadata.put("Content-Type", List.of(page.getContentType().split(";")[0]));

            // Add the result to our list.
            extractions.add(new DocumentExtraction(url, metadata, textContent));
        }
    }

    private String getDomainName(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return ""; // Return empty string for invalid URLs
        }
    }

	@Override
	public KairoSpider newInstance() throws Exception {
		return this;
	}
}