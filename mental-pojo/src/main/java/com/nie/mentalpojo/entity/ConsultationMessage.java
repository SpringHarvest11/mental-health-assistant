package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationMessage {

    //会话消息id
    private Integer id;

    //会话id
    private Integer sessionId;

    //发送者类型 1:用户 2:AI助手
    private Integer senderType;

    //消息内容
    private String content;

    //消息类型 1:文本
    private Integer messageType;

    //情绪标签
    private String emotionTag;

    //使用的ai模型
    private String aiModel;

    //创建时间
    private LocalDateTime createdAt;

}
