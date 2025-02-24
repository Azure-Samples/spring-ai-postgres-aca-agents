package com.example.springaiapp.shell;

import com.example.springaiapp.service.RagService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Console commands for the RAG (Retrieval Augmented Generation) demo.
 * This class demonstrates how to:
 * 1. Use Spring Shell for interactive console applications
 * 2. Integrate RAG functionality in a simple way
 * 3. Provide user-friendly command-line interface
 */
@ShellComponent
public class RagDemoCommands {
    private final RagService ragService;
    
    public RagDemoCommands(RagService ragService) {
        this.ragService = ragService;
    }
    
    /**
     * Main command to ask questions using RAG.
     * Example: ask "What is Spring AI?"
     * 
     * The system will:
     * 1. Convert your question to embeddings
     * 2. Find similar previous Q&As
     * 3. Use them as context for generating an answer
     * 4. Store the new Q&A pair for future reference
     */
    @ShellMethod(key = "ask", value = "Ask a question using RAG")
    public String ask(@ShellOption(help = "Your question") String question) {
        return ragService.processQuery(question);
    }
    
    /**
     * Help command to explain available functionality
     */
    @ShellMethod(key = "help", value = "Show help information")
    public String help() {
        return """
            Spring AI RAG Demo - Teaching Example
            
            This is a simple demonstration of RAG (Retrieval Augmented Generation) using:
            - Spring AI for Azure OpenAI integration
            - PGVector for vector similarity search
            - Spring Shell for console interface
            
            Available commands:
            - ask 'your question'    Ask a question using RAG
            - help                   Show this help message
            - exit                   Exit the application
            
            Example:
            ask What is Spring AI?
            
            How it works:
            1. Your question is converted to a vector using Azure OpenAI
            2. Similar previous Q&As are found using vector search
            3. These similar Q&As provide context for the AI
            4. A new answer is generated and stored for future use
            """;
    }
}
