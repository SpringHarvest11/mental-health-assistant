package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;

public class ConversationDTO {

    //会话id
    private Integer sessionId;

    //用户消息内容
    @NotBlank(message = "")
    private String userMessage;
}
