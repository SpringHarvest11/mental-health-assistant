package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationSessionStartVO {

    //会话 ID，建议格式 数字id
    private Integer sessionId;

    //会话状态，正式会话建议返回 ACTIVE，不能返回 TEMP
    private String status;
}
