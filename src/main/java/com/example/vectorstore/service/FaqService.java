package com.example.vectorstore.service;

import com.example.vectorstore.model.FaqItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for reading and parsing FAQ data from text files
 */
@Slf4j
@Service
public class FaqService {

    private final ResourceLoader resourceLoader;
    private final String faqFilePath;

    public FaqService(ResourceLoader resourceLoader,
                      @Value("${app.faq.file-path}") String faqFilePath) {
        this.resourceLoader = resourceLoader;
        this.faqFilePath = faqFilePath;
    }

    /**
     * Reads FAQ items from the configured text file
     *
     * @return List of FAQ items
     * @throws IOException if file cannot be read
     */
    public List<FaqItem> loadFaqItems() throws IOException {
        log.info("Loading FAQ items from file: {}", faqFilePath);

        Resource resource = resourceLoader.getResource(faqFilePath);
        if (!resource.exists()) {
            throw new IOException("FAQ file not found: " + faqFilePath);
        }

        List<FaqItem> faqItems = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                FaqItem faqItem = FaqItem.fromTextLine(line);
                if (faqItem != null && !faqItem.getQuestion().isEmpty()) {
                    faqItems.add(faqItem);
                }
            }
        }

        log.info("Loaded {} FAQ items from file", faqItems.size());
        return faqItems;
    }
}
