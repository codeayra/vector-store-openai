# Spring Boot Vector Store FAQ System

This Spring Boot application demonstrates how to use Spring AI with a vector store to create an intelligent FAQ system. The application reads FAQ data from a text file, stores it in a SimpleVectorStore for semantic search, and provides AI-powered responses through REST endpoints using Retrieval-Augmented Generation (RAG).

## Features

- **FAQ Data Management**: Reads FAQ data from a text file with structured format
- **Vector Store Integration**: Uses Spring AI's SimpleVectorStore for semantic search with persistent storage
- **AI-Powered Responses**: Integrates with OpenAI to provide intelligent answers based on FAQ context using RAG
- **RESTful API**: Provides endpoints for searching FAQs and asking questions
- **Semantic Search**: Finds relevant FAQ items using vector similarity with configurable thresholds
- **Prompt Templates**: Uses customizable prompt templates for AI responses
- **Persistent Vector Store**: Saves and loads vector store data to avoid rebuilding on each restart

## Prerequisites

- Java 17 or later
- OpenAI API key
- Gradle (for building the project)

## Setup

1. **Clone or download the project**

2. **Set your OpenAI API key**:
   - Set the `OPENAI_API_KEY` environment variable, or
   - Update the `spring.ai.openai.api-key` property in `application.properties`

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Search FAQs
```
GET /api/faq/search?query={your-query}&topK={number}
```
Searches for similar FAQ items using semantic similarity.

**Parameters**:
- `query` (required): The search query
- `topK` (optional): Maximum number of results (default: 5)

**Example**:
```
GET /api/faq/search?query=How to create REST API&topK=3
```

**Response**: Returns a list of FAQ items with question, answer, and category.

### 2. Ask a Question (AI-Powered)
```
GET /api/faq/ask?query={your-question}&topK={number}
```
Returns an AI-generated answer based on relevant FAQ context using RAG (Retrieval-Augmented Generation).

**Parameters**:
- `query` (required): The question to ask
- `topK` (optional): Number of FAQ items to use as context (default: 3)

**Example**:
```
GET /api/faq/ask?query=How do I create a REST API in Spring Boot?&topK=3
```

**Response**:
```json
{
  "answer": "AI-generated answer based on FAQ context...",
  "relevantFaqs": [
    {
      "question": "How do I create a REST API in Spring Boot?",
      "answer": "You can create a REST API by using...",
      "category": "Spring Boot"
    }
  ],
  "query": "How do I create a REST API in Spring Boot?"
}
```

## FAQ File Format

The FAQ data is stored in `src/main/resources/faq.txt` with the following format:
```
Q: Question text | A: Answer text | Category: Category name
```

**Example**:
```
Q: What is Spring Boot? | A: Spring Boot is a framework that simplifies Spring application development. | Category: Spring Boot
```

## Configuration

Key configuration properties in `application.properties`:

- `spring.ai.openai.api-key`: Your OpenAI API key (can be set via environment variable `OPENAI_API_KEY`)
- `spring.ai.openai.chat.options.model`: OpenAI model to use (default: gpt-5-nano)
- `app.faq.file-path`: Path to the FAQ text file (default: classpath:/docs/faq.txt)
- `app.vectorstore.similarity-threshold`: Similarity threshold for vector search (default: 0.7)
- `app.vectorstore.file-name`: Vector store file name for persistence (default: vectorstore.json)

### Environment Variables
- `OPENAI_API_KEY`: Your OpenAI API key (alternative to setting in application.properties)

### Logging Configuration
- `logging.level.com.example`: Logging level for application classes (default: INFO)
- `logging.level.org.springframework.ai`: Logging level for Spring AI (default: INFO)

## How It Works

1. **Initialization**: On startup, the application checks for existing vector store data
2. **Vector Store Creation**: If no vector store exists, FAQ items are read from the text file and converted to documents
3. **Embedding**: Each FAQ item is embedded using OpenAI's embedding model and stored in SimpleVectorStore
4. **Persistence**: Vector store data is saved to disk for faster subsequent startups
5. **Search**: When a query is made, the system finds similar FAQ items using vector similarity with configurable thresholds
6. **RAG Response**: Relevant FAQ context is combined with a prompt template and sent to OpenAI to generate intelligent responses
7. **Token Splitting**: Large documents are automatically split using TokenTextSplitter to handle token limits

## Example Usage

### Search for Similar FAQs
```bash
curl "http://localhost:8080/api/faq/search?query=authentication&topK=3"
```

### Ask a Question (AI-Powered)
```bash
curl "http://localhost:8080/api/faq/ask?query=How%20do%20I%20handle%20security%20in%20Spring%20Boot?&topK=3"
```

## Project Structure

```
src/main/java/com/example/vectorstore/
├── VectorStoreApplication.java          # Main Spring Boot application class
├── controller/
│   └── FaqController.java              # REST controller for FAQ operations
├── model/
│   └── FaqItem.java                    # FAQ data model with parsing logic
└── service/
    ├── FaqService.java                 # FAQ file reading and parsing service
    └── VectorStoreService.java         # Vector store management and search

src/main/resources/
├── application.properties              # Application configuration
├── docs/
│   ├── faq.txt                        # Main FAQ data file
│   └── olympic-faq.txt                # Additional FAQ data (optional)
├── prompts/
│   └── faq.st                         # Prompt template for AI responses
└── data/
    ├── vectorstore.json               # Persistent vector store data
    └── olympic-vectorstore.json       # Olympic FAQ vector store data
```

## Dependencies

- Spring Boot 3.5.5
- Spring AI 1.0.1
- Spring AI OpenAI Starter
- Spring AI Vector Store
- Lombok (for reducing boilerplate code)
- Gradle (build tool)
- JUnit 5 (for testing)

## Notes

- The application uses Spring AI version 1.0.1 (stable release)
- Vector store data is persisted to disk and loaded on startup for faster initialization
- The application supports multiple FAQ data sources (main FAQ and Olympic FAQ)
- Prompt templates are customizable and located in `src/main/resources/prompts/`
- For production use, consider using external vector databases like Pinecone or Weaviate
- Make sure to set your OpenAI API key before running the application
- The application uses RAG (Retrieval-Augmented Generation) for intelligent responses
- Token splitting is automatically handled for large documents
