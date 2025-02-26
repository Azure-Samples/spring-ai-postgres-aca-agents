package com.example.springaiapp.controller;

import com.example.springaiapp.service.BlogWriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blog")
public class BlogWriterController {

    private final BlogWriterService blogWriterService;

    @Autowired
    public BlogWriterController(BlogWriterService blogWriterService) {
        this.blogWriterService = blogWriterService;
    }

    @GetMapping
    public String generateBlogPost(@RequestParam String topic) {
        return blogWriterService.generateBlogPost(topic);
    }
}
