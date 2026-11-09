package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class ConsultationSessionStartDTO {

    //用户的第一条消息
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息最多2000个字符")
    private String initialMessage;


    //会话标题
    @Size(max = 200, message = "会话标题最多200个字符")
    private String sessionTitle;
}
