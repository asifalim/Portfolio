package com.alim.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

// ─── Chat Request ─────────────────────────────────────
@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 2000, message = "Message too long (max 2000 chars)")
    private String message;

    private List<MessageHistory> history;

    @Data
    public static class MessageHistory {
        private String role;   // "user" | "assistant"
        private String content;
    }
}
