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
public class AiAnalysisTask {

    //任务ID
    private Integer id;

    //日记ID
    private Integer diaryId;

    //用户ID
    private Integer userId;

    //任务状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败
    private String status;

    //任务类型：AUTO-自动触发，MANUAL-手动触发，ADMIN-管理员触发，BATCH-批量触发
    private String taskType;

    //优先级：1-低，2-正常，3-高，4-紧急（默认2）
    private Integer priority;

    //重试次数
    private Integer retryCount;

    //最大重试次数（默认3）
    private Integer maxRetryCount;

    //错误信息
    private String errorMessage;

    //处理开始时间
    private LocalDateTime startedAt;

    //处理完成时间
    private LocalDateTime completedAt;

    //创建时间
    private LocalDateTime createdAt;

    //更新时间
    private LocalDateTime updatedAt;
}
