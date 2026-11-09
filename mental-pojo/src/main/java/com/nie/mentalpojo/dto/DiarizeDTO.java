package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiarizeDTO {

    //记录日期  YYYY-MM-DD，默认今天
    @NotNull(message = "日期不能为空")
    private LocalDate diaryDate;

    //情绪评分 1~10
    @NotNull(message = "请选择评分")
    private Integer moodScore;

    //主要情绪，取值见数据字典
    private String dominantEmotion;

    //情绪触发因素，≤ 1000 字
    @Size(max = 2000, message = "情绪触发因素最多2000个字符")
    private String emotionTriggers;

    //今日感想，≤ 2000 字
    @Size(max = 4000, message = "今日感想最多4000个字符")
    private String diaryContent;

    //睡眠质量 1~5
    private Integer sleepQuality;

    //压力水平 1~5
    private Integer stressLevel;

}
