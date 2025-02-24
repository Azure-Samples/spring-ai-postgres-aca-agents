package com.example.springaiapp.model;

/**
 * Represents a chat interaction with its vector embedding.
 * 
 * This model is used to store:
 * - User prompts (questions)
 * - AI responses
 * - Vector embeddings for semantic similarity search
 * 
 * The embeddings are stored as PostgreSQL vectors (1536 dimensions)
 * which allows for efficient similarity search using the pgvector extension.
 */
public class ChatHistory {
    private Long id;
    private String prompt;
    private String response;
    private double[] embedding;
    
    public ChatHistory() {}
    
    public ChatHistory(String prompt, String response, double[] embedding) {
        this.prompt = prompt;
        this.response = response;
        this.embedding = embedding;
    }
    
    // Constructor for database results
    public ChatHistory(Long id, String prompt, String response, double[] embedding) {
        this.id = id;
        this.prompt = prompt;
        this.response = response;
        this.embedding = embedding;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }
    
    /**
     * Converts the embedding array to a PostgreSQL vector string.
     * Example: [1.0, 2.0, 3.0] -> '[1.0,2.0,3.0]'
     */
    public String getEmbeddingAsString() {
        if (embedding == null) return null;
        return "[" + String.join(",", java.util.Arrays.stream(embedding)
            .mapToObj(String::valueOf)
            .toArray(String[]::new)) + "]";
    }
}
