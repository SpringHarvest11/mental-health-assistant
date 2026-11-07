package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.ConsultationMessage;
import com.nie.mentalpojo.entity.ConsultationSession;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PsychologicalChatMapper {

    /**
     *创建会话记录
     * @param consultationSession
     */
    @Insert("insert into consultation_session (user_id, session_title, started_at, last_emotion_analysis, last_emotion_updated_at)" +
                             " values(#{userId},#{sessionTitle},#{startedAt},#{lastEmotionAnalysis},#{lastEmotionUpdatedAt})")
    @Options(useGeneratedKeys = true, keyProperty ="id", keyColumn ="id")
    void insertSession(ConsultationSession consultationSession);

    /**
     *记录会话第一条消息
     * @param consultationMessage
     */
    @Insert("insert into consultation_message (session_id, sender_type, message_type, content, emotion_tag,ai_model, created_at)" +
                            " values(#{sessionId},#{senderType},#{messageType},#{content},#{emotionTag},#{aiModel},#{createdAt})")
    void insertMessage(ConsultationMessage consultationMessage);

    /**
     * 管理员获取会话记录（看全部会话，带用户昵称）
     * @return
     */
    List<PageSessionMessageVO> listAdminSessions();

    /**
     * 用户获取会话记录(普通用户：只看自己的会话)
     * @return
     */
    List<PageSessionMessageVO> listUserSessions(Integer userId);

    /**
     * 根据会话id获取会话记录
     * @param sessionId
     * @return
     */
    List<HistoricalMessagesVO> getMessagesBySessionId(Integer sessionId, Integer userId,Integer userType);

    /**
     * 根据会话id删除会话记录
     * @param sessionId
     */
    @Delete("delete from consultation_session where id = #{sessionId} and user_id = #{userId}")
    int deleteSessionById(Integer sessionId, Integer userId);

    /**
     * 根据会话id删除关联历史会话记录
     * @param sessionId
     */
    @Delete("delete from consultation_message where session_id = #{sessionId} ")
    void deleteMessageBySessionId(Integer sessionId);
}
