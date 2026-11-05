package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.ConsultationMessage;
import com.nie.mentalpojo.entity.ConsultationSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

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
    @Insert("insert into consultation_message (session_id, sender_type, message_type, context, emotion_tag,ai_model, created_at)" +
                            " values(#{sessionId},#{senderType},#{messageType},#{context},#{emotionTag},#{aiModel},#{createdAt})")
    void insertMessage(ConsultationMessage consultationMessage);
}
