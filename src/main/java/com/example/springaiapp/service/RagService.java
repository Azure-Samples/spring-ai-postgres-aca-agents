package com.example.springaiapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import java.util.List;
import java.util.Map;
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
    
    @Value("${spring.ai.azure.openai.chat.options.deployment-name}")
    private String chatDeploymentName;
    
    @Value("${spring.ai.azure.openai.embedding.options.deployment-name}")
    private String embeddingDeploymentName;
        
    @Autowired
    VectorStore vectorStore;
    
    public RagService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    @PostConstruct
    private void init() {
        logger.info("RagService initialized with chat deployment: {}, embedding deployment: {}", 
                   chatDeploymentName, embeddingDeploymentName);
    }
    
    public String processQuery(String query) {
        try {
            logger.debug("Processing query: {}", query);
            
            // Step 1: Find similar previous Q&As
            logger.debug("Finding similar contexts");

            List<Document> similarContexts = vectorStore.similaritySearch(SearchRequest.builder().query(query).similarityThreshold(0.8).topK(3).build());
            logger.debug("Found {} similar contexts", similarContexts.size());
            
            // Step 2: Build prompt with context from similar Q&As
            String context = similarContexts.stream()
                .map(ch -> String.format("Q: %s\nA: %s", ch.getMetadata().get("prompt"), ch.getText()))
                .collect(Collectors.joining("\n\n"));
                
            logger.debug("Built context with {} characters", context.length());


            String promptText = String.format("""
                Use these previous Q&A pairs as context for answering the new question:
                
                Previous interactions:
                %s
                
                New question: %s
                
                Please provide a clear and educational response.""",
                context,
                query
            );

            // Step 3: Generate AI response with system context
            logger.debug("Generating response using chat deployment: {}", chatDeploymentName);
            SystemMessage systemMessage = new SystemMessage(
                "You are a helpful AI assistant that provides clear and educational responses."
            );
            UserMessage userMessage = new UserMessage(promptText);
            
            logger.debug("Sending prompt to Azure OpenAI");
            ChatResponse response = chatClient.prompt().messages(List.of(systemMessage, userMessage)).call().chatResponse();
            String answer = response.getResult().getOutput().getText();
            logger.debug("Received response of {} characters", answer.length());
            
            // Step 4: Save interaction for future context
            logger.debug("Saving interaction to repository");
            vectorStore.add(List.of(new Document(answer, Map.of("prompt", query))));
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
