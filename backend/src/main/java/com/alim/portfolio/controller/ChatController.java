package com.alim.portfolio.controller;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.OllamaResponse;
import com.alim.portfolio.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "http://localhost:4202")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Alex's AI Agent chatbot powered by Claude")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a message to Alex's AI Agent",
               description = "Returns a personality-driven response about Alex's background, skills, and experience")
    public OllamaResponse chat(@Valid @RequestBody ChatRequest request) {
        log.debug("Chat request received: {}", request.getMessage());
      return chatService.chat(request);
    }
}
