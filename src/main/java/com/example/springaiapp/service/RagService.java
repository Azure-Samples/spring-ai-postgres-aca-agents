package com.example.springaiapp.service;

import com.example.springaiapp.model.ChatHistory;
import com.example.springaiapp.repository.ChatHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
// import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.springframework.ai.document.Document;
import java.util.List;
// import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

/**
 * RAG (Retrieval Augmented Generation) Service
 * 
 * This service demonstrates a simple RAG implementation:
 * 1. Convert input query to embeddings
 * 2. Find similar previous Q&As using vector similarity
 * 3. Use found Q&As as context for the AI
 * 4. Generate and store new responses
 * 
 * This approach helps the AI:
 * - Give more relevant answers
 * - Learn from previous interactions
 * - Maintain consistency across responses
 * 
 * Educational Note:
 * RAG enhances AI responses by providing relevant context from previous interactions.
 * This is particularly useful for:
 * - Maintaining consistency in responses
 * - Providing domain-specific knowledge
 * - Reducing hallucinations by grounding responses in real data
 */
@Service
public class RagService {
    private static final Logger logger = LoggerFactory.getLogger(RagService.class);
    
    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;
    private final ChatHistoryRepository repository;
    
    @Value("${spring.ai.azure.openai.chat.options.deployment-name}")
    private String chatDeploymentName;
    
    @Value("${spring.ai.azure.openai.embedding.options.deployment-name}")
    private String embeddingDeploymentName;
        
    //@Autowired
    //VectorStore vectorStore;

    // List<Document> documents = List.of(
    //     new Document("3e1a1af7-c872-4e36-9faa-fe53b9613c69",
    //                  "At Microsoft, Java is used by various teams and in multiple projects, " +
    //                  "particularly in areas such as cloud services (e.g., Azure), " +
    //                  "enterprise applications (e.g., LinkedIn), and cross-platform development (e.g., Minecraft). " +
    //                  "Developers working on applications that require integration with Java-based solutions " + 
    //                  "or those building on Java ecosystems may utilize Java as part of their technology stack. " +
    //                  "Additionally, Microsoft provides support for Java developers through tools " +
    //                  "and services, including Azure SDKs for Java and integration with Visual Studio Code. " +
    //                  "Java is also prominent in open-source projects and contributions made by Microsoft, " +
    //                  "reflecting its versatility and importance in the software development landscape.",
    //                  Map.of("prompt", "Who uses Java in Microsoft?")));
    
    public RagService(
            ChatClient.Builder chatClientBuilder,
            EmbeddingService embeddingService,
            ChatHistoryRepository repository) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingService = embeddingService;
        this.repository = repository;
    }
    
    @PostConstruct
    private void init() {
        //vectorStore.add(documents);
        logger.info("RagService initialized with chat deployment: {}, embedding deployment: {}", 
                   chatDeploymentName, embeddingDeploymentName);
    }
    
    public String processQuery(String query) {
        try {
            logger.debug("Processing query: {}", query);
            
            // Step 1: Generate embedding for semantic search
            logger.debug("Generating embedding using deployment: {}", embeddingDeploymentName);
            double[] queryEmbedding = embeddingService.generateEmbedding(query);
            logger.debug("Generated embedding of size: {}", queryEmbedding.length);
            
            // Step 2: Find similar previous Q&As
            logger.debug("Finding similar contexts");

            List<ChatHistory> similarContexts = repository.findNearestNeighbors(queryEmbedding, 3);
            logger.debug("Found {} similar contexts", similarContexts.size());
            // List<Document> similarContexts = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(3).build());
            // logger.debug("Found {} similar contexts", similarContexts);
            
            // Step 3: Build prompt with context from similar Q&As
            String context = similarContexts.stream()
                .map(ch -> String.format("Q: %s\nA: %s", ch.getPrompt(), ch.getResponse()))
                //.map(ch -> String.format("%s", ch.getText()))
                .collect(Collectors.joining("\n\n"));
                
            logger.debug("Built context with {} characters", context.length());

            //                Use these relevant documents as context for answering the new question:                
            //                Relevant documents:
            String promptText = String.format("""
                Use these previous Q&A pairs as context for answering the new question:
                
                Previous interactions:
                %s
                
                New question: %s
                
                Please provide a clear and educational response.""",
                context,
                query
            );
            
            // Step 4: Generate AI response with system context
            logger.debug("Generating response using chat deployment: {}", chatDeploymentName);
            SystemMessage systemMessage = new SystemMessage(
                "You are a helpful AI assistant that provides clear and educational responses."
            );
            UserMessage userMessage = new UserMessage(promptText);
            
            logger.debug("Sending prompt to Azure OpenAI");
            ChatResponse response = chatClient.prompt().messages(List.of(systemMessage, userMessage)).call().chatResponse();
            String answer = response.getResult().getOutput().getText();
            logger.debug("Received response of {} characters", answer.length());
            
            // Step 5: Save interaction for future context
            logger.debug("Saving interaction to repository");
            repository.save(new ChatHistory(query, answer, queryEmbedding));
            logger.debug("Successfully saved interaction");
            
            return answer;
            
        } catch (Exception e) {
            logger.error("Error processing query: {}", query, e);
            String errorMessage = String.format(
                "Error processing query. Deployment info - Chat: %s, Embedding: %s. Error: %s",
                chatDeploymentName,
                embeddingDeploymentName,
                e.getMessage()
            );
            return errorMessage;
        }
    }
}
