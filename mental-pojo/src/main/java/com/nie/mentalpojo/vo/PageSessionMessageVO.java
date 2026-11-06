package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageSessionMessageVO {

    //会话数字 ID（消息、删除接口使用
    private Integer id;

    //会话标题
    private String sessionTitle;

    //	开始时间
    private LocalDateTime startedAt;

    //最后消息时间
    private LocalDateTime lastMessageTime;

    //最后一条消息内容
    private String lastMessageContent;

    //消息数量
    private Integer messageCount;

    //会话时长（分钟）
    private Integer durationMinutes;

    //用户昵称
    private String userNickname;

}
