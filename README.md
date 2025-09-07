# Spring Boot Vector Store FAQ System

This Spring Boot application demonstrates how to use Spring AI with a vector store to create an intelligent FAQ system. The application reads FAQ data from a text file, stores it in a SimpleVectorStore for semantic search, and provides AI-powered responses through REST endpoints.

## Features

- **FAQ Data Management**: Reads FAQ data from a text file with structured format
- **Vector Store Integration**: Uses Spring AI's SimpleVectorStore for semantic search
- **AI-Powered Responses**: Integrates with OpenAI to provide intelligent answers based on FAQ context
- **RESTful API**: Provides endpoints for searching FAQs and asking questions
- **Semantic Search**: Finds relevant FAQ items using vector similarity

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

**Example**:
```
GET /api/faq/search?query=How to create REST API&topK=3
```

### 2. Ask a Question (AI-Powered)
```
POST /api/faq/ask
Content-Type: application/json

{
  "question": "How do I create a REST API in Spring Boot?"
}
```
Returns an AI-generated answer based on relevant FAQ context.

### 3. Get All Categories
```
GET /api/faq/categories
```
Returns all available FAQ categories.

### 4. Get FAQs by Category
```
GET /api/faq/category/{category}
```
Returns all FAQ items in a specific category.

### 5. Get All FAQs
```
GET /api/faq/all
```
Returns all FAQ items.

### 6. Health Check
```
GET /api/faq/health
```
Returns the application status and FAQ count.

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

- `spring.ai.openai.api-key`: Your OpenAI API key
- `spring.ai.openai.chat.options.model`: OpenAI model to use (default: gpt-3.5-turbo)
- `app.faq.file-path`: Path to the FAQ text file
- `app.vectorstore.similarity-threshold`: Similarity threshold for vector search (default: 0.7)

## How It Works

1. **Initialization**: On startup, the application reads FAQ data from the text file
2. **Vector Store**: FAQ items are converted to documents and stored in a SimpleVectorStore
3. **Embedding**: Each FAQ item is embedded using OpenAI's embedding model
4. **Search**: When a query is made, the system finds similar FAQ items using vector similarity
5. **AI Response**: Relevant FAQ context is sent to OpenAI to generate intelligent responses

## Example Usage

### Search for Similar FAQs
```bash
curl "http://localhost:8080/api/faq/search?query=authentication&topK=3"
```

### Ask a Question
```bash
curl -X POST "http://localhost:8080/api/faq/ask" \
  -H "Content-Type: application/json" \
  -d '{"question": "How do I handle security in Spring Boot?"}'
```

## Project Structure

```
src/main/java/com/example/vectorstore/
├── VectorStoreApplication.java          # Main application class
├── controller/
│   └── FaqController.java              # REST controller
├── model/
│   └── FaqItem.java                    # FAQ data model
└── service/
    ├── FaqService.java                 # FAQ file reading service
    └── VectorStoreService.java         # Vector store management
```

## Dependencies

- Spring Boot 3.2.0
- Spring AI 1.0.0-M3
- Lombok (for reducing boilerplate code)
- Gradle (build tool)

## Notes

- The application uses Spring AI's milestone version (1.0.0-M3)
- Vector store is in-memory and will be rebuilt on each restart
- For production use, consider using a persistent vector database
- Make sure to set your OpenAI API key before running the application
