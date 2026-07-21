package com.alim.portfolio.controller;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.OllamaResponse;
import com.alim.portfolio.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Alim's AI Agent chatbot")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a message to Alim's AI Agent",
               description = "Returns a personality-driven response about Alim's background, skills, and experience")
    public OllamaResponse chat(@Valid @RequestBody ChatRequest request) {
        log.debug("Chat request received: {}", request.getMessage());
      return chatService.chat(request);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a message to Alim's AI Agent",
               description = "Returns a stream of text chunks representing the AI response")
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.debug("Stream chat request received: {}", request.getMessage());
        return chatService.streamChat(request);
    }
}
