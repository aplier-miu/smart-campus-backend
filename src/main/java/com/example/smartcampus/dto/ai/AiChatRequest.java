package com.example.smartcampus.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequest {
    @NotBlank(message = "问题不能为空")
    @Size(max = 300, message = "问题不能超过300字")
    private String question;
}