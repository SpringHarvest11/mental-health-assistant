package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalMessagesVO {

    //消息id
    private Integer id;

    //消息来源  1 用户，2 AI
    private Integer senderType;

    //消息内容
    private String content;

    //发送时间
    private LocalDateTime createdAt;
}
