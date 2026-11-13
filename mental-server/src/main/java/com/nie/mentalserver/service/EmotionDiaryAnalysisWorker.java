package com.nie.mentalserver.service;

import com.nie.mentalpojo.entity.EmotionDiary;
import com.nie.mentalpojo.vo.SessionEmotionVO;
import com.nie.mentalserver.mapper.AiAnalysisTaskMapper;
import com.nie.mentalserver.mapper.EmotionDiaryMapper;
import com.nie.mentalserver.util.EmotionAnalysisNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EmotionDiaryAnalysisWorker {

    private static final int BATCH_SIZE = 10;
    private static final int STALE_MINUTES = 10;
    private static final int ERROR_MAX_LENGTH = 500;

    private final AiAnalysisTaskMapper aiAnalysisTaskMapper;
    private final EmotionDiaryMapper emotionDiaryMapper;
    private final EmotionDiaryAnalysisWriteBackService writeBackService;
    private final EmotionAnalysisNormalizer emotionAnalysisNormalizer;
    private final ObjectMapper objectMapper;
    private final ChatClient diaryEmotionChatClient;
    private final ThreadPoolTaskExecutor executor;

    // 防止上一次轮询还没派发完，下一次又进来
    private final AtomicBoolean polling = new AtomicBoolean(false);

    public EmotionDiaryAnalysisWorker(
            AiAnalysisTaskMapper aiAnalysisTaskMapper,
            EmotionDiaryMapper emotionDiaryMapper,
            EmotionDiaryAnalysisWriteBackService writeBackService,
            EmotionAnalysisNormalizer emotionAnalysisNormalizer,
            ObjectMapper objectMapper,
            @Qualifier("diaryEmotionChatClient") ChatClient diaryEmotionChatClient,
            @Qualifier("diaryAnalysisExecutor") ThreadPoolTaskExecutor executor) {
        this.aiAnalysisTaskMapper = aiAnalysisTaskMapper;
        this.emotionDiaryMapper = emotionDiaryMapper;
        this.writeBackService = writeBackService;
        this.emotionAnalysisNormalizer = emotionAnalysisNormalizer;
        this.objectMapper = objectMapper;
        this.diaryEmotionChatClient = diaryEmotionChatClient;
        this.executor = executor;
    }

    @Scheduled(fixedDelayString = "${app.diary-analysis.poll-interval-ms:30000}")
    public void poll() {
        if (!polling.compareAndSet(false, true)) {
            return;
        }
        try {
            // 回收卡在 PROCESSING 的任务
            aiAnalysisTaskMapper.recoverStale(
                    LocalDateTime.now().minusMinutes(STALE_MINUTES));

            List<Integer> taskIds = aiAnalysisTaskMapper.listPendingIds(BATCH_SIZE);
            for (Integer taskId : taskIds) {
                try {
                    executor.execute(() -> process(taskId));
                } catch (TaskRejectedException e) {
                    log.warn("分析线程池已满，任务保持待处理: taskId={}", taskId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("扫描情绪日记分析任务失败", e);
        } finally {
            polling.set(false);
        }
    }

    private void process(Integer taskId) {
        try {
            // 条件领取，多线程/多实例下只有一个能成功
            if (aiAnalysisTaskMapper.claim(taskId) == 0) {
                return;
            }

            // 读取送模型时的日记快照；查不到说明已被删除或任务已失效
            EmotionDiary diary = emotionDiaryMapper.getActiveDiaryByTaskId(taskId);
            if (diary == null) {
                aiAnalysisTaskMapper.markOrphan(taskId, "日记已删除");
                return;
            }

            String prompt = buildPrompt(diary);

            // 事务外调用模型
            SessionEmotionVO vo = diaryEmotionChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(SessionEmotionVO.class);

            if (vo == null) {
                throw new IllegalStateException("模型返回为空");
            }

            // 模型输出不可信，统一归一化后再序列化
            String analysisJson = objectMapper.writeValueAsString(
                    emotionAnalysisNormalizer.normalize(vo));

            writeBackService.writeBack(taskId, diary, analysisJson);

        } catch (Exception e) {
            log.error("情绪日记分析失败: taskId={}", taskId, e);
            aiAnalysisTaskMapper.fail(taskId, truncate(e.getMessage(), ERROR_MAX_LENGTH));
        }
    }

    private String buildPrompt(EmotionDiary d) {
        return """
                请分析下面这篇情绪日记。
                日期：%s
                情绪评分（1-10）：%s
                主要情绪：%s
                情绪触发因素：%s
                睡眠质量（1-5）：%s
                压力水平（1-5）：%s
                日记正文：
                %s
                """.formatted(
                d.getDiaryDate(),
                d.getMoodScore(),
                text(d.getDominantEmotion()),
                text(d.getEmotionTriggers()),
                d.getSleepQuality(),
                d.getStressLevel(),
                truncate(d.getDiaryContent(), 4000));
    }

    private String text(String value) {
        return StringUtils.hasText(value) ? value : "无";
    }

    private String truncate(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return "无";
        }
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }
}
