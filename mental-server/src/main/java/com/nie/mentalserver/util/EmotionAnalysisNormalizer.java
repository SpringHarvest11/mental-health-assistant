package com.nie.mentalserver.util;

import com.nie.mentalpojo.vo.SessionEmotionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/**
 * AI 情绪分析结果的解析与规范化
 */
@Slf4j
@Component
public class EmotionAnalysisNormalizer {

    private final ObjectMapper objectMapper;

    public EmotionAnalysisNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 把 JSON 字符串规范化为字段完整、取值合法的 JSON 字符串；为空或解析失败时返回 null
     */
    public String format(String json) {
        SessionEmotionVO vo = parse(json);
        if (vo == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (Exception e) {
            log.warn("序列化情绪分析结果失败", e);
            return null;
        }
    }

    /**
     * 解析并规范化，为空或解析失败时返回 null
     */
    public SessionEmotionVO parse(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return normalize(objectMapper.readValue(json, SessionEmotionVO.class));
        } catch (Exception e) {
            log.warn("解析情绪分析结果失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 补齐缺失字段、裁剪越界值
     */
    public SessionEmotionVO normalize(SessionEmotionVO vo) {
        SessionEmotionVO result = vo == null ? SessionEmotionVO.neutral() : vo;

        if (!StringUtils.hasText(result.getPrimaryEmotion())) {
            result.setPrimaryEmotion("中性");
        }
        int score = result.getEmotionScore() == null ? 50
                : Math.max(0, Math.min(100, result.getEmotionScore()));
        result.setEmotionScore(score);

        int risk = result.getRiskLevel() == null ? 0
                : Math.max(0, Math.min(3, result.getRiskLevel()));
        result.setRiskLevel(risk);

        if (result.getIsNegative() == null) {
            result.setIsNegative(risk > 0 || score < 40);
        }
        if (result.getImprovementSuggestions() == null) {
            result.setImprovementSuggestions(new ArrayList<>());
        }
        if (!StringUtils.hasText(result.getRiskDescription())) {
            result.setRiskDescription("暂未发现明显风险。");
        }
        if (!StringUtils.hasText(result.getSuggestion())) {
            result.setSuggestion("先别急着评判自己，允许情绪存在也是一种照顾。");
        }
        if (risk >= 3) {
            result.setSuggestion("你现在的状态需要被认真对待，请立即联系身边信任的人、学校心理中心，"
                    + "或拨打心理援助热线。");
        }
        return result;
    }
}
