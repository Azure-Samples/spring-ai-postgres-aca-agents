package com.example.springaiapp.controller;

import com.example.springaiapp.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    @Autowired
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping
    public String processQuery(@RequestParam String query) {
        return ragService.processQuery(query);
    }
}
