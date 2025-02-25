package com.example.springaiapp.repository;

import com.example.springaiapp.model.ChatHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for storing and retrieving chat history with vector embeddings.
 * Uses PostgreSQL's pgvector extension for vector similarity search.
 */
@Repository
public class ChatHistoryRepository {
    private final JdbcTemplate jdbcTemplate;
    
    private final RowMapper<ChatHistory> rowMapper = (rs, rowNum) -> {
        String embeddingStr = rs.getString("embedding");
        double[] embedding = null;
        if (embeddingStr != null) {
            // Parse JSON array string to double[]
            String[] values = embeddingStr
                .replace("[", "")
                .replace("]", "")
                .split(",");
            embedding = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                embedding[i] = Double.parseDouble(values[i].trim());
            }
        }
            
        return new ChatHistory(
            rs.getLong("id"),
            rs.getString("prompt"),
            rs.getString("response"),
            embedding
        );
    };
    
    public ChatHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Save a new chat interaction with its embedding.
     */
    public ChatHistory save(ChatHistory history) {
        String sql = """
            INSERT INTO chat_history (prompt, response, embedding)
            VALUES (?, ?, ?)
            RETURNING id, prompt, response, embedding
            """;
            
        return jdbcTemplate.queryForObject(sql, rowMapper,
            history.getPrompt(),
            history.getResponse(),
            history.getEmbeddingAsString()
        );
    }
    
    /**
     * Find similar chat interactions using vector similarity search.
     * Uses L2 distance (Euclidean) to find nearest neighbors.
     * 
     * @param embedding Query vector
     * @param k Number of results to return
     * @return List of similar chat interactions
     */
    public List<ChatHistory> findNearestNeighbors(double[] queryEmbedding, int k) {
        // Fetch all records and compute similarity in memory since we can't use pgvector
        String sql = "SELECT id, prompt, response, embedding FROM chat_history";
        List<ChatHistory> allHistory = jdbcTemplate.query(sql, rowMapper);
        
        // Sort by cosine similarity
        return allHistory.stream()
            .filter(h -> h.getEmbedding() != null) // Filter out any null embeddings
            .sorted((a, b) -> Double.compare(
                cosineSimilarity(b.getEmbedding(), queryEmbedding),
                cosineSimilarity(a.getEmbedding(), queryEmbedding)))
            .limit(k)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private double cosineSimilarity(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
