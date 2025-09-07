package com.example.vectorstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a FAQ item with question and answer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqItem {
    
    private String question;
    private String answer;
    private String category;
    
    /**
     * Creates a FAQ item from a text line
     * Expected format: "Q: Question text | A: Answer text | Category: Category name"
     */
    public static FaqItem fromTextLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = line.split("\\|");
        if (parts.length < 2) {
            return null;
        }
        
        String question = extractValue(parts[0], "Q:");
        String answer = extractValue(parts[1], "A:");
        String category = parts.length > 2 ? extractValue(parts[2], "Category:") : "General";
        
        return new FaqItem(question, answer, category);
    }
    
    private static String extractValue(String text, String prefix) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith(prefix)) {
            return trimmed.substring(prefix.length()).trim();
        }
        return trimmed;
    }
    
    /**
     * Returns the content for vector embedding (question + answer)
     */
    public String getContent() {
        return String.format("Question: %s\nAnswer: %s", question, answer);
    }
}
