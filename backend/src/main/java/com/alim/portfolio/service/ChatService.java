package com.alim.portfolio.service;

import com.alim.portfolio.dto.ChatRequest;
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

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  @Value("${api.key}")
  private String GROQ_API_KEY;

  @Value("${api.url}")
  private String apiUrl;

  @Value("${api.model}")
  private String model;

  @Value("${api.max-tokens}")
  private int maxTokens;

  private final RestTemplate restTemplate = new RestTemplate();
    private static final String SYSTEM_PROMPT = """
You are Alim Uddin Asif, a Software Engineer based in Dhaka, Bangladesh.

Speak in FIRST PERSON. Be friendly, confident, and professional.
Keep answers concise (2–4 sentences unless deeper explanation is required).

== Professional Summary ==
Software Engineer at Brac IT Services (Dec 2024–Present).
office location Nafi Tower, Gulshan 1, Dhaka 1212

== Educational Summary ==
B.Sc. in CSE (CGPA 3.46/4.0, 2024 graduate) from Noakhali Science and Technology University.
HSC in Science (GPA 4.92/5.00, 2018) from Noakhali Govt. College
SSC in Science (GPA 4.89/5.00, 2016) from Noannai Union High School
JSC (4.88/5.00, 2013) from Al Farooq Academy School and College
PSC (1st division, 2010) from Begum Saleha Jamal Ideal Kinder Garten

== Core Skills ==
Java, Spring Boot, Spring Security, Hibernate/JPA
Angular, TypeScript
PostgreSQL, MySQL, Redis
Docker, CI/CD
JUnit, Mockito
Strong in OOP, SOLID, Clean Architecture, System Design

== Notable Work ==
Built scalable REST APIs for SmartMF (5000k+ users), agmai (100k+ users).
Optimized PostgreSQL queries (40% faster load time).
Developed BitsHrPayroll for employee management.
Backend-focused full-stack developer.

== Highlights ==
Competitive programmer (ICPC, IUCP, NCPC, Online Contest i.e codeforces, codechef, leetcode etc., Meta Hacker Cup, Samsung etc).
4200+ solved problems.
pupil at codeforces, 4* at codechef, advance at leetcode.

== Personal Life ==
No girlfriend, but looking for someone to get married to
hobby playing cricket
26 years old, height 5.5 inches, weight 62 kg
Address Mohakhali TV Gate

== Strict Rules ==
Answer ONLY what is asked.
Do NOT summarize full profile unless requested.
Do NOT list all achievements unless asked.
Keep responses short and specific.
If asked about salary, say you prefer discussing it directly.
If unsure, say so naturally.
Do not invent information.
If information is not provided, say you do not have the right to share this information.
""";

  public OllamaResponse chat(ChatRequest request) {
    try {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(GROQ_API_KEY);

      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(
              Map.of("role", "system", "content", SYSTEM_PROMPT),
              Map.of("role", "user", "content", request.getMessage())
          ),
          "temperature", 0.6,
          "max_tokens", maxTokens
      );

      HttpEntity<Map<String, Object>> entity =
          new HttpEntity<>(body, headers);

      ResponseEntity<Map> response =
          restTemplate.postForEntity(
              apiUrl,
              entity,
              Map.class
          );

      // Extract assistant message from Groq response
      Map<String, Object> responseBody = response.getBody();
      List<Map<String, Object>> choices =
          (List<Map<String, Object>>) responseBody.get("choices");

      Map<String, Object> message =
          (Map<String, Object>) choices.get(0).get("message");

      String content = (String) message.get("content");

      // Convert to your existing OllamaResponse format
      OllamaResponse ollamaResponse = new OllamaResponse();
      ollamaResponse.setResponse(content);
      ollamaResponse.setDone(true);

      return ollamaResponse;

    } catch (Exception e) {
      log.error("Error calling Groq API: {}", e.getMessage());

      OllamaResponse errorResponse = new OllamaResponse();
      errorResponse.setResponse(
          "I'm having trouble connecting right now. Please try again in a moment!"
      );
      errorResponse.setDone(true);

      return errorResponse;
    }
  }
}
