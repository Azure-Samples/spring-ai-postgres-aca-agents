package com.example.springaiapp.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for pre-loading documents stored using PG Vector. 
 */
@Service
public class DocumentService {
 
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    VectorStore vectorStore;

    @PostConstruct
    private void init() {
        vectorStore.add(documents);
        logger.info("DocumentService initialized with. Document count: {}", 
                   documents.size());
    }

    List<Document> documents = List.of(
        new Document("3e1a1af7-c872-4e36-9faa-fe53b9613c69",
                    """
                    At Microsoft, Java is used by various teams and in multiple projects, particularly in areas such as cloud services (e.g., Azure), 
                    enterprise applications (e.g., LinkedIn), and cross-platform development (e.g., Minecraft). Developers working on applications that require 
                    integration with Java-based solutions or those building on Java ecosystems may utilize Java as part of their technology stack. 
                    Additionally, Microsoft provides support for Java developers through tools and services, including Azure SDKs for Java and integration with Visual Studio Code. 
                    Java is also prominent in open-source projects and contributions made by Microsoft, reflecting its versatility and importance in the software development landscape.
                    """,
                     Map.of("prompt", "Who uses Java in Microsoft?")),
        new Document("7a7c2caf-ce9c-4dcb-a543-937b76ef1098", 
                    """
                    A good migration path for ASA (Azure Spring Apps) customers is to use Azure Container Apps (ACA) as it is a more modern and flexible platform for running Spring Boot applications.
                    """,
                     Map.of("prompt", "What is a good migration path for ASA customers?")),
        new Document("9174be57-e549-4f59-9c3d-417d99b78b86", 
                    """
                    Vector similarity search finds similar items by comparing their vector embeddings using distance metrics like cosine similarity or Euclidean distance. 
                    In RAG, it helps find relevant context from previous interactions
                    """,
                     Map.of("prompt", "What is vector similarity search?")));

}
