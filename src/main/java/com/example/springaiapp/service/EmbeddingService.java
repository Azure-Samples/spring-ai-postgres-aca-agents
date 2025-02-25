package com.example.springaiapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

/**
 * Service for generating text embeddings using Azure OpenAI.
 * 
 * Embeddings are vector representations of text that capture semantic meaning.
 * Similar texts will have similar embedding vectors, which allows for:
 * - Semantic search
 * - Finding similar documents
 * - Clustering related content
 */
@Service
public class EmbeddingService {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    
    @Autowired
    private AzureOpenAiEmbeddingModel embeddingModel;
    
    @Value("${spring.ai.azure.openai.embedding.options.deployment-name}")
    private String embeddingDeploymentName;
    
    @PostConstruct
    private void init() {
        logger.info("EmbeddingService initialized with deployment: {}", embeddingDeploymentName);
    }
    
    public double[] generateEmbedding(String text) {
        try {
            logger.debug("Generating embedding for text of length: {} using deployment: {}", 
                        text.length(), embeddingDeploymentName);
            
            float[] embedding = embeddingModel.embed(text);
            
            double[] result = new double[embedding.length];
            for (int i = 0; i < embedding.length; i++) {
                result[i] = embedding[i];
            }
            
            logger.debug("Successfully generated embedding of size: {}", result.length);
            return result;
        } catch (Exception e) {
            logger.error("Error generating embedding with deployment {}: {}", 
                        embeddingDeploymentName, e.getMessage(), e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }
}
