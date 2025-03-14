package com.example.springaiapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class BlogWriterService {
    private static final Logger logger = LoggerFactory.getLogger(BlogWriterService.class);
    private static final int MAX_ITERATIONS = 3;

    private final ChatClient chatClient;

    public BlogWriterService(ChatClient.Builder chatClientBuilder) {
        // Add SimpleLoggerAdvisor to log requests and responses
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        logger.info("BlogWriterService initialized with ChatClient and SimpleLoggerAdvisor");
    }

    public String generateBlogPost(String topic) {
        logger.info("Starting blog generation for topic: {}", topic);

        // Writer: Generate initial blog draft
        String initialPrompt = String.format("""
            You are a professional blog writer. Write a well-structured, engaging blog post about "%s".
            The post should have a clear introduction, body paragraphs, and conclusion.
            Include relevant examples and maintain a conversational yet professional tone.
            """, topic);
        
        logger.info("Sending initial draft generation prompt to AI model");
        String draft = chatClient.prompt()
                .user(initialPrompt)
                .call()
                .content();
        logger.info("Initial draft successfully generated for topic: {}", topic);

        // Enter evaluator-optimizer loop for refinement
        boolean approved = false;
        int iteration = 1;
        
        while (!approved && iteration <= MAX_ITERATIONS) {
            logger.info("Starting iteration {} of blog refinement", iteration);
            
            // Editor: Evaluate the current draft
            String evalPrompt = String.format("""
                You are a critical blog editor. Evaluate the following blog draft and respond with either:
                PASS - if the draft is well-written, engaging, and complete
                NEEDS_IMPROVEMENT - followed by specific, actionable feedback on what to improve
                
                Focus on:
                - Clarity and flow of ideas
                - Engagement and reader interest
                - Professional yet conversational tone
                - Structure and organization
                
                Draft:
                %s
                """, draft);
            
            logger.info("Sending draft for editorial evaluation (iteration: {})", iteration);
            String evaluation = chatClient.prompt()
                    .user(evalPrompt)
                    .call()
                    .content();
            
            if (evaluation.toUpperCase().contains("PASS")) {
                approved = true;
                logger.info("Draft approved by editor on iteration {}", iteration);
            } else {
                // Extract feedback and refine the draft
                String feedback = extractFeedback(evaluation);
                logger.info("Editor feedback received (iteration {}): {}", iteration, feedback);
                
                // Writer: Refine the draft using feedback
                String refinePrompt = String.format("""
                    You are a blog writer. Improve the following blog draft based on this editorial feedback:
                    
                    Feedback: %s
                    
                    Current Draft:
                    %s
                    
                    Provide the complete improved version while maintaining the original topic and structure.
                    """, feedback, draft);
                
                logger.info("Requesting draft revision based on feedback (iteration: {})", iteration);
                draft = chatClient.prompt()
                        .user(refinePrompt)
                        .call()
                        .content();
                logger.info("Revised draft received for iteration {}", iteration);
            }
            iteration++;
        }

        if (!approved) {
            logger.warn("Maximum iterations ({}) reached without editor approval", MAX_ITERATIONS);
        } else {
            logger.info("Blog post generation completed successfully for topic: {}", topic);
        }

        return draft;
    }

    private String extractFeedback(String evaluation) {
        if (evaluation == null) return "";
        int idx = evaluation.toUpperCase().indexOf("NEEDS_IMPROVEMENT");
        if (idx != -1) {
            // Return text after "NEEDS_IMPROVEMENT"
            return evaluation.substring(idx + "NEEDS_IMPROVEMENT".length()).trim();
        }
        return evaluation;
    }
} 