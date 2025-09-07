package com.example.vectorstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Vector Store FAQ system
 * <p>
 * This application demonstrates:
 * - Reading FAQ data from a text file
 * - Storing FAQ data in a SimpleVectorStore for semantic search
 * - Using Spring AI to provide intelligent responses based on FAQ context
 * - RESTful API endpoints for FAQ operations
 */
@SpringBootApplication
public class VectorStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VectorStoreApplication.class, args);
    }
}
