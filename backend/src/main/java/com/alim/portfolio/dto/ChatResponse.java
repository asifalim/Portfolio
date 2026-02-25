package com.alim.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String role;
    private boolean success;
    private String error;

    public static ChatResponse success(String message) {
        return ChatResponse.builder()
                .message(message)
                .role("assistant")
                .success(true)
                .build();
    }

    public static ChatResponse error(String error) {
        return ChatResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}
