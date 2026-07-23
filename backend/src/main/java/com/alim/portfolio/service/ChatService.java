package com.alim.portfolio.service;

import com.alim.portfolio.dto.ChatRequest;
import com.alim.portfolio.dto.GroqRequest;
import com.alim.portfolio.dto.GroqResponse;
import com.alim.portfolio.dto.GroqStreamResponse;
import com.alim.portfolio.dto.OllamaResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
import com.fasterxml.jackson.databind.ObjectMapper;

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
  private final ObjectMapper objectMapper = new ObjectMapper();

  // ─── Conversation History Builder ────────────────────────────────────────
  // LLMs are stateless — every API call is independent and the model has no
  // memory of prior calls. "Memory" is achieved by replaying the conversation
  // history in each request: [system, turn1-user, turn1-assistant, ..., new-user].
  //
  // Windowing: we keep the last MAX_HISTORY_TURNS pairs to avoid exceeding the
  // model's context window and to keep costs/latency reasonable.
  private static final int MAX_HISTORY_TURNS = 10; // 10 pairs = 20 messages

  private List<GroqRequest.Message> buildMessages(ChatRequest request) {
    List<GroqRequest.Message> messages = new ArrayList<>();

    // 1. System prompt always goes first — sets the AI's persona/instructions
    messages.add(GroqRequest.Message.builder()
        .role("system")
        .content(systemPrompt)
        .build());

    // 2. Inject conversation history (if any) — this is what gives the AI memory
    if (request.getHistory() != null && !request.getHistory().isEmpty()) {
      List<ChatRequest.HistoryMessage> history = request.getHistory();

      // Windowing: take only the last MAX_HISTORY_TURNS turns to limit context size.
      // Each "turn" is one user message + one assistant reply = 2 messages.
      int startIndex = Math.max(0, history.size() - (MAX_HISTORY_TURNS * 2));
      history.subList(startIndex, history.size()).forEach(h ->
          messages.add(GroqRequest.Message.builder()
              .role(h.getRole())
              .content(h.getContent())
              .build())
      );
    }

    // 3. The current user message goes last
    messages.add(GroqRequest.Message.builder()
        .role("user")
        .content(request.getMessage())
        .build());

    return messages;
  }

  public Flux<String> streamChat(ChatRequest request) {
    GroqRequest body = GroqRequest.builder()
        .model(model)
        .messages(buildMessages(request))  // ← full history + current message
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
                GroqStreamResponse response = objectMapper.readValue(json, GroqStreamResponse.class);
                String token = response.getContent();
                if (token == null || token.isEmpty()) return null;
                // Wrap in a JSON object to avoid SSE serialization ambiguity (quotes, etc.)
                return "{\"t\":" + objectMapper.writeValueAsString(token) + "}";
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
          .messages(buildMessages(request))  // ← full history + current message
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
