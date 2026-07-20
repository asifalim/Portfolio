package com.alim.portfolio.service;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.GroqRequest;
import com.alim.portfolio.dto.GroqResponse;
import com.alim.portfolio.dto.OllamaResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
