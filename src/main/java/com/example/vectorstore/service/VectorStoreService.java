package com.example.vectorstore.service;

import com.example.vectorstore.model.FaqItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing FAQ data in a vector store for semantic search
 */
@Slf4j
@Service
public class VectorStoreService {

    private final FaqService faqService;
    private final double similarityThreshold;
    private final String vectorStoreFileName;

    private final SimpleVectorStore vectorStore;

    private final SimpleVectorStore vectorStoreOlympic;

    @Value("classpath:/docs/faq.txt")
    private Resource faqResource;

    @Value("classpath:/docs/olympic-faq.txt")
    private Resource olympicFaqResource;


    public VectorStoreService(FaqService faqService,
                              EmbeddingModel embeddingModel,
                              @Value("${app.vectorstore.similarity-threshold:0.7}") double similarityThreshold,
                              @Value("${app.vectorstore.file-name:vectorstore.json}") String vectorStoreFilePath) {
        this.faqService = faqService;
        this.similarityThreshold = similarityThreshold;
        this.vectorStoreFileName = vectorStoreFilePath;
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        this.vectorStoreOlympic = SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * Initializes the vector store with FAQ data
     */
    @PostConstruct
    public void initializeVectorStore() {
        log.info("Creating new vector store from FAQ data...");
        //only for testing
        //storeOlympicFaqInVector();
        File vectorStoreFile = getVectorStoreFile();
        //already vector store file exists
        if (vectorStoreFile.exists()) {
            vectorStore.load(vectorStoreFile);
        } else {
            // Load FAQ items and convert to documents
            List<Document> documents = getDocuments(true);
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            // Split documents if they exceed token limit (e.g., 1000 tokens)
            List<Document> splitDocuments = textSplitter.apply(documents);
            // Add documents to vector store
            vectorStore.add(splitDocuments);
            vectorStore.save(vectorStoreFile);

            log.info("Vector store initialized with {} FAQ documents", documents.size());
        }

    }

    private void storeOlympicFaqInVector() {
        File vectorStoreFileOlympic = getVectorStoreFileOlympic();
        if (!vectorStoreFileOlympic.exists()) {
            //store olympic faq also
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            TextReader textReader = new TextReader(olympicFaqResource);
            textReader.getCustomMetadata().put("source", "olympic-faq.txt");
            List<Document> documentsOlympics = textReader.get();
            // Split documents if they exceed token limit (e.g., 1000 tokens)
            List<Document> splitDocumentsOlympics = textSplitter.apply(documentsOlympics);
            // Add documents to vector store
            // Add documents to vector store
            vectorStoreOlympic.add(splitDocumentsOlympics);
            vectorStoreOlympic.save(vectorStoreFileOlympic);
        }
    }

    private List<Document> getDocuments(boolean manual) {
        List<Document> documents = new ArrayList<>();
        if (manual) {
            List<FaqItem> faqItems = null;
            try {
                faqItems = faqService.loadFaqItems();
            } catch (IOException e) {
                log.error("Failed to initialize vector store", e);
                throw new RuntimeException("Failed to initialize vector store", e);
            }
            documents = faqItems.stream()
                    .map(this::createDocument)
                    .toList();
        } else {
            TextReader textReader = new TextReader(faqResource);
            textReader.getCustomMetadata().put("source", "faq.txt");
            documents = textReader.get();
        }
        return documents;
    }

    /**
     * Creates a Document from a FAQ item
     */
    private Document createDocument(FaqItem faqItem) {
        return new Document(
                faqItem.getContent(),
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "question", faqItem.getQuestion(),
                        "answer", faqItem.getAnswer(),
                        "category", faqItem.getCategory()
                )
        );
    }

    /**
     * Searches for similar FAQ items based on a query
     *
     * @param query The search query
     * @param topK  Maximum number of results to return
     * @return List of similar documents
     */
    public List<Document> searchSimilar(String query, int topK) {
        if (vectorStore == null) {
            throw new IllegalStateException("Vector store not initialized");
        }

        log.debug("Searching for similar documents with query: {}", query);

        SearchRequest searchRequest = SearchRequest.builder().query(query)
                .topK(topK).similarityThreshold(similarityThreshold).build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        log.debug("Found {} similar documents", results.size());
        return results;
    }

    /**
     * Searches for similar FAQ items and returns them as FAQ items
     *
     * @param query The search query
     * @param topK  Maximum number of results to return
     * @return List of similar FAQ items
     */
    public List<FaqItem> searchSimilarFaqItems(String query, int topK) {
        List<Document> documents = searchSimilar(query, topK);
        return documents.stream()
                .map(this::documentToFaqItem)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Document back to a FAQ item
     */
    private FaqItem documentToFaqItem(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new FaqItem(
                (String) metadata.get("question"),
                (String) metadata.get("answer"),
                (String) metadata.get("category")
        );
    }

    private File getVectorStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsolutePath() + File.separator + vectorStoreFileName;
        return new File(absolutePath);
    }

    private File getVectorStoreFileOlympic() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsolutePath() + File.separator + "olympic-" + vectorStoreFileName;
        return new File(absolutePath);
    }


}
