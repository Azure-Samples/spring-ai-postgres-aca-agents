package com.example.springaiapp.controller;

import com.example.springaiapp.service.BlogWriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/blog")
public class BlogWriterController {

    private final BlogWriterService blogWriterService;

    @Autowired
    public BlogWriterController(BlogWriterService blogWriterService) {
        this.blogWriterService = blogWriterService;
    }

    @GetMapping(produces = "application/json")
    public Map<String, Object> generateBlogPost(@RequestParam String topic) {
        // Generate the blog post and capture metadata
        BlogWriterService.BlogGenerationResult result = blogWriterService.generateBlogPostWithMetadata(topic);
        
        // Create a structured JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("topic", topic);
        response.put("content", result.getContent());
        response.put("metadata", createMetadataObject(result));
        
        return response;
    }
    
    private Map<String, Object> createMetadataObject(BlogWriterService.BlogGenerationResult result) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("iterations", result.getIterations());
        metadata.put("approved", result.isApproved());
        metadata.put("totalTokensUsed", result.getTotalTokens());
        
        if (result.getEditorFeedback() != null && !result.getEditorFeedback().isEmpty()) {
            List<Map<String, Object>> feedbackHistory = new ArrayList<>();
            for (int i = 0; i < result.getEditorFeedback().size(); i++) {
                Map<String, Object> feedbackEntry = new HashMap<>();
                feedbackEntry.put("iteration", i + 1);
                feedbackEntry.put("feedback", result.getEditorFeedback().get(i));
                feedbackHistory.add(feedbackEntry);
            }
            metadata.put("editorFeedback", feedbackHistory);
        }
        
        // Include token usage statistics if available
        if (result.getPromptTokens() > 0) {
            Map<String, Object> tokenUsage = new HashMap<>();
            tokenUsage.put("promptTokens", result.getPromptTokens());
            tokenUsage.put("completionTokens", result.getCompletionTokens());
            tokenUsage.put("totalTokens", result.getTotalTokens());
            metadata.put("tokenUsage", tokenUsage);
        }
        
        // Include model information if available
        if (result.getModelName() != null) {
            metadata.put("model", result.getModelName());
        }
        
        return metadata;
    }
}
