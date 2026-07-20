package com.alim.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroqRequest {

  private String model;
  private List<Message> messages;
  private double temperature;

  @JsonProperty("max_tokens")
  private int maxTokens;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Message {
    private String role; // "system" or "user" or "assistant"
    private String content;
  }

}
