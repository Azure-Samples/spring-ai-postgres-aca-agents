package com.example.springaiapp.shell;

import com.example.springaiapp.service.BlogWriterService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class BlogWriterCommand {
    private final BlogWriterService blogWriterService;

    public BlogWriterCommand(BlogWriterService blogWriterService) {
        this.blogWriterService = blogWriterService;
    }

    @ShellMethod(
        key = "write-blog",
        value = """
            Generate a blog post using an AI writer-editor feedback loop.
            The system uses two AI roles:
            1. Writer - Generates and refines the blog content
            2. Editor - Evaluates and provides feedback
            
            The process continues for up to 3 iterations or until the editor approves.
            Progress and feedback are logged to the console.
            
            Example: write-blog "Spring AI Integration with Azure OpenAI"
            """
    )
    public String writeBlog(
            @ShellOption(
                help = """
                    The topic for your blog post. Use quotes for topics with spaces.
                    Example: "Spring AI and Azure OpenAI Integration in 3 sentences"
                    """
            ) String topic
    ) {
        return blogWriterService.generateBlogPost(topic);
    }
} 