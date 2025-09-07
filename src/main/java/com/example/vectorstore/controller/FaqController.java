package com.example.vectorstore.controller;

import com.example.vectorstore.model.FaqItem;
import com.example.vectorstore.service.FaqService;
import com.example.vectorstore.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for FAQ operations and AI-powered query responses
 */
@Slf4j
@RestController
@RequestMapping("/api/faq")
public class FaqController {

    private final VectorStoreService vectorStoreService;
    private final ChatClient chatClient;

    @Value("classpath:prompts/faq.st")
    private Resource ragPromptTemplate;

    public FaqController(ChatClient.Builder chatClientBuilder, VectorStoreService vectorStoreService) {
        this.chatClient = chatClientBuilder
                .defaultOptions(ChatOptions.builder().temperature(1.0).build())
                .build();
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * Search for FAQ items using semantic similarity
     *
     * @param query The search query
     * @param topK  Maximum number of results (default: 5)
     * @return List of similar FAQ items
     */
    @GetMapping("/search")
    public ResponseEntity<List<FaqItem>> searchFaq(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {

        log.info("Searching FAQ with query: {}, topK: {}", query, topK);

        try {
            List<FaqItem> results = vectorStoreService.searchSimilarFaqItems(query, topK);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching FAQ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get AI-powered answer based on FAQ context
     *
     * @param query
     * @param topK  Number of FAQ items to use as context (default: 3)
     * @return AI-generated response
     */
    @GetMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question is required"));
        }

        log.info("Processing question: {}", query);

        try {
            // Find relevant FAQ items
            List<FaqItem> relevantFaqs = vectorStoreService.searchSimilarFaqItems(query, topK);

            if (relevantFaqs.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "answer", "I couldn't find relevant information in our FAQ database.",
                        "relevantFaqs", List.of()
                ));
            }

            // Create context from relevant FAQs
            Prompt prompt = getPrompt(query, relevantFaqs);

            String aiAnswer = chatClient.prompt(prompt)
                    .call()
                    .content();

            Map<String, Object> result = new HashMap<>();
            result.put("answer", aiAnswer);
            result.put("relevantFaqs", relevantFaqs);
            result.put("query", query);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error processing question", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process question: " + e.getMessage()));
        }
    }

    private Prompt getPrompt(String query, List<FaqItem> relevantFaqs) {
        String context = getContext(relevantFaqs);
        PromptTemplate template = new PromptTemplate(ragPromptTemplate);
        return template.create(Map.of("context", context, "question", query));
    }

    private String getContext(List<FaqItem> relevantFaqs) {
        StringBuilder context = new StringBuilder();
        context.append("Based on the following FAQ information, please provide a helpful answer:\n\n");

        for (int i = 0; i < relevantFaqs.size(); i++) {
            FaqItem faq = relevantFaqs.get(i);
            context.append(String.format("FAQ %d:\n", i + 1));
            context.append(String.format("Q: %s\n", faq.getQuestion()));
            context.append(String.format("A: %s\n\n", faq.getAnswer()));
        }
        return context.toString();
    }

}
