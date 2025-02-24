package com.example.springaiapp.service;

import com.example.springaiapp.model.ChatHistory;
import com.example.springaiapp.repository.ChatHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
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
    
    public RagService(
            @Qualifier("azureOpenAiChatClient") ChatClient chatClient,
            EmbeddingService embeddingService,
            ChatHistoryRepository repository) {
        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }
    
    @PostConstruct
    private void init() {
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
            
            // Step 3: Build prompt with context from similar Q&As
            String context = similarContexts.stream()
                .map(ch -> String.format("Q: %s\nA: %s", ch.getPrompt(), ch.getResponse()))
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
            
            // Step 4: Generate AI response with system context
            logger.debug("Generating response using chat deployment: {}", chatDeploymentName);
            SystemMessage systemMessage = new SystemMessage(
                "You are a helpful AI assistant that provides clear and educational responses."
            );
            UserMessage userMessage = new UserMessage(promptText);
            
            logger.debug("Sending prompt to Azure OpenAI");
            ChatResponse response = chatClient.call(new Prompt(List.of(systemMessage, userMessage)));
            String answer = response.getResult().getOutput().getContent();
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
