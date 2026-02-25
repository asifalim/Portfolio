package com.alim.portfolio.service;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.OllamaRequest;
import com.alim.portfolio.dto.OllamaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  @Value("${anthropic.api.key}")
  private String apiKey;

  @Value("${anthropic.api.url}")
  private String apiUrl;

  @Value("${anthropic.api.model}")
  private String model;

  @Value("${anthropic.api.max-tokens}")
  private int maxTokens;

  private final RestTemplate restTemplate = new RestTemplate();
    private static final String SYSTEM_PROMPT = """
You are Alim Uddin Asif, a Software Engineer based in Dhaka, Bangladesh.

Speak in FIRST PERSON. Be friendly, confident, and professional.
Keep answers concise (2–4 sentences unless deeper explanation is required).

== Professional Summary ==
Software Engineer at Brac IT Services (Dec 2024–Present).
B.Sc. in CSE (CGPA 3.46/4.0, 2024 graduate).

Backend-focused full-stack developer.

== Core Skills ==
Java, Spring Boot, Spring Security, Hibernate/JPA
Angular, TypeScript
PostgreSQL, MySQL, Redis
Docker, CI/CD
JUnit, Mockito
Strong in OOP, SOLID, Clean Architecture, System Design

== Notable Work ==
Built scalable REST APIs for SmartMF (50k+ users).
Optimized PostgreSQL queries (40% faster load time).
Developed Angular admin dashboards.

== Highlights ==
Competitive programmer (ICPC, Meta Hacker Cup).
4200+ solved problems.
Codeforces Pupil, CodeChef 4*.

== Strict Rules ==
- Answer ONLY what is asked.
- Do NOT summarize full profile unless requested.
- Do NOT list all achievements unless asked.
- Keep responses short and specific.
- If asked about salary, say you prefer discussing it directly.
- If unsure, say so naturally.
""";

  public OllamaResponse chat(ChatRequest request) {
    try {
        String fullPrompt = """
%s

User Question:
%s

Answer concisely:
""".formatted(SYSTEM_PROMPT, request.getMessage());

        OllamaRequest requestBody =
            new OllamaRequest(model, fullPrompt, false);

        requestBody.setOptions(Map.of(
            "num_predict", 100,   //prevents long essays
            "temperature", 0.6,      //controlled but natural
            "top_p", 0.9,           //avoids repeating profile
            "repeat_penalty", 1.1      //smooth generation
        ));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<OllamaRequest> entity =
          new HttpEntity<>(requestBody, headers);

      ResponseEntity<OllamaResponse> response =
          restTemplate.postForEntity(
              apiUrl,
              entity,
              OllamaResponse.class
          );

      assert response.getBody() != null;
      return response.getBody();

    } catch (Exception e) {
      log.error("Error calling Claude API: {}", e.getMessage());
      OllamaResponse ollamaResponse = new OllamaResponse();
      ollamaResponse.setResponse(
          "I'm having trouble connecting right now. Please try again in a moment!");
      ollamaResponse.setDone(true);
      return ollamaResponse;
    }
  }

  private List<Map<String, String>> buildMessages(ChatRequest request) {
    List<Map<String, String>> messages = new ArrayList<>();

    // Add conversation history
    if (request.getHistory() != null) {
      messages.addAll(request.getHistory().stream()
          .map(h -> Map.of("role", h.getRole(), "content", h.getContent()))
          .toList());
    }

    // Add current user message
    messages.add(Map.of("role", "user", "content", request.getMessage()));
    return messages;
  }

  @SuppressWarnings("unchecked")
  private String extractReply(Map response) {
    try {
      List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
      if (content != null && !content.isEmpty()) {
        return (String) content.get(0).get("text");
      }
    } catch (Exception e) {
      log.error("Error extracting reply: {}", e.getMessage());
    }
    return "I couldn't process that response. Please try again!";
  }
}
