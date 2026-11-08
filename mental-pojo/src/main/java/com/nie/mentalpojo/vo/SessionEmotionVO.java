package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话情绪分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEmotionVO {

    //主要情绪
    private String primaryEmotion;

    //情绪强度 0~100
    private Integer emotionScore;

    //是否负面情绪：必须是包装类型 Boolean，用 boolean 会序列化成 negative
    private Boolean isNegative;

    //风险等级 0正常 1关注 2预警 3危机
    private Integer riskLevel;

    //风险描述
    private String riskDescription;

    //温暖建议
    private String suggestion;

    //改善建议列表，前端直接取 length，不能为 null
    private List<String> improvementSuggestions;

    /**
     * 兜底/空会话返回值
     */
    public static SessionEmotionVO neutral() {
        return SessionEmotionVO.builder()
                .primaryEmotion("中性")
                .emotionScore(50)
                .isNegative(false)
                .riskLevel(0)
                .riskDescription("暂未发现明显风险。")
                .suggestion("慢慢说，你的感受值得被听见。")
                .improvementSuggestions(new ArrayList<>())
                .build();
    }
}
