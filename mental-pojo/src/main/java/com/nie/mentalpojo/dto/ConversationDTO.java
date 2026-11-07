package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConversationDTO {

    //会话id
    @NotNull(message = "会话id不能为空")
    private Integer sessionId;

    //用户消息内容
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息最多1000个字符")
    private String userMessage;
}
