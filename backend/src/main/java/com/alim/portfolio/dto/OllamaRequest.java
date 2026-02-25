package com.alim.portfolio.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OllamaRequest {
  private String model;
  private String prompt;
  private boolean stream;
  private Map<String, Object> options;

  public OllamaRequest(String model, String prompt, boolean stream) {
    this.model = model;
    this.prompt = prompt;
    this.stream = stream;
  }
}
