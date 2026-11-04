package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSession {

    //会话id
    private Integer id;

    //用户id
    private Integer userId;

    //会话标题
    private String sessionTitle;

    //最后一次情绪分析结果(JSON格式)
    private String lastEmotionAnalysis;

    //开始时间
    private LocalDateTime startedAt;

    //最后一次情绪分析更新时间
    private LocalDateTime lastEmotionUpdatedAt;
}
