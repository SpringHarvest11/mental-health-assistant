package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.AiAnalysisTask;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiAnalysisTaskMapper {

    /**
     * 删除情绪日记对应的AI任务分析表
     * @param diaryId
     */
    @Delete("delete from ai_analysis_task where diary_id = #{diaryId}")
    void deleteAiAnalysisTaskByDiaryId(Integer diaryId);

    /**
     * 向AI任务分析表中插入一条数据
     * @param task
     * @return
     */
    int insert(AiAnalysisTask task);

    /**
     * 获取AI任务分析表中limit条的id（通过priority，created_at升序排序）
     * @param limit
     * @return
     */
    List<Integer> listPendingIds(@Param("limit") int limit);

    /**
     * 将AI任务分析表中的PENDING-待处理任务状态变为PROCESSING-处理中
     * @param id
     * @return
     */
    int claim(@Param("id") Integer id);

    /**
     * 将AI任务分析表中的PROCESSING-处理中任务状态变为COMPLETED-已完成
     * @param id
     * @return
     */
    int complete(@Param("id") Integer id);

    /**
     *
     * @param id
     * @param error
     * @return
     */
    int fail(@Param("id") Integer id, @Param("error") String error);

    /**
     * 处理失败与重试（PROCESSING-处理中任务状态任务失败后，先校验是否还有重复次数，有返回PENDING状态，没有则置为FAILED-失败）
     * @param id
     * @param error
     * @return
     */
    int markOrphan(@Param("id") Integer id, @Param("error") String error);

    /**
     * 处理卡住的AI分析任务，将卡住的任务置为FAILED-失败或PENDING-待处理
     * @param deadline
     * @return
     */
    int recoverStale(@Param("deadline") LocalDateTime deadline);
}