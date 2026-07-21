package com.alim.portfolio.service;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.GroqRequest;
import com.alim.portfolio.dto.GroqResponse;
import com.alim.portfolio.dto.OllamaResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  @Value("${api.key}")
  private String groqApiKey;

  @Value("${api.url}")
  private String apiUrl;

  @Value("${api.model}")
  private String model;

  @Value("${api.max-tokens}")
  private int maxTokens;

  @Value("${api.temperature}")
  private double temperature;

  @Value("classpath:prompts/system-prompts.txt")
  private Resource systemPromptResource;

  private String systemPrompt;

  @PostConstruct
  public void loadSystemPrompt() throws IOException {
    this.systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
  }

  private final RestTemplate restTemplate;
  private final WebClient.Builder webClientBuilder;

  public Flux<String> streamChat(ChatRequest request) {
    GroqRequest body = GroqRequest.builder()
        .model(model)
        .messages(List.of(
            GroqRequest.Message.builder()
                .role("system")
                .content(systemPrompt)
                .build(),
            GroqRequest.Message.builder()
                .role("user")
                .content(request.getMessage())
                .build()
        ))
        .temperature(temperature)
        .maxTokens(maxTokens)
        .stream(true)
        .build();

    return webClientBuilder.build().post()
        .uri(apiUrl)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToFlux(String.class)
        .filter(line -> !line.trim().isEmpty() && !line.contains("[DONE]"))
        .mapNotNull(line -> {
            String json = line;
            if (json.startsWith("data: ")) {
                json = json.substring(6);
            } else if (json.startsWith("data:")) {
                json = json.substring(5);
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.alim.portfolio.dto.GroqStreamResponse response = mapper.readValue(json, com.alim.portfolio.dto.GroqStreamResponse.class);
                String token = response.getContent();
                if (token == null || token.isEmpty()) return null;
                // Wrap in a JSON object to avoid SSE serialization ambiguity (quotes, etc.)
                return "{\"t\":" + mapper.writeValueAsString(token) + "}";
            } catch (Exception e) {
                log.error("Error parsing stream chunk: {}", e.getMessage());
                return null;
            }
        })
        .filter(Objects::nonNull);
  }

  public OllamaResponse chat(ChatRequest request) {
    try {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON); // Set the content type to JSON
      headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Set the acceptance type to JSON, but here it is optional as groq returns JSON by default
      headers.setBearerAuth(groqApiKey);

      GroqRequest body = GroqRequest.builder()
          .model(model)
          .messages(List.of(
              GroqRequest.Message.builder()
                  .role("system")
                  .content(systemPrompt)
                  .build(),
              GroqRequest.Message.builder()
                  .role("user")
                  .content(request.getMessage())
                  .build()
          ))
          .temperature(temperature)
          .maxTokens(maxTokens)
          .build();

      HttpEntity<GroqRequest> entity =
          new HttpEntity<>(body, headers);

      ResponseEntity<GroqResponse> response =
          restTemplate.postForEntity(
              apiUrl,
              entity,
              GroqResponse.class
          );

      // Extract assistant message from Groq response
      GroqResponse responseBody = response.getBody();
      String content = (responseBody != null) ? responseBody.getFirstMessageContent() : "No response from AI";

      // Convert to your existing OllamaResponse format
      return new OllamaResponse(content, true);

    } catch (Exception e) {
      log.error("Error calling Groq API: {}", e.getMessage());

      return new OllamaResponse(
          "I'm having trouble connecting right now. Please try again in a moment!",
          true
      );
    }
  }
}
