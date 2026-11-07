package com.nie.mentalserver.service;

import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.dto.ConversationDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface PsychologicalChatService {

    /**
     * 创建会话实现
     * @param consultationSessionStartDTO
     * @return
     */
    ConsultationSessionStartVO sessionStart(ConsultationSessionStartDTO consultationSessionStartDTO);

    /**
     * 获取管理员会话记录（分页展示）
     * @param page
     * @param size
     * @return
     */
    PageResult<PageSessionMessageVO> getAdminSessions(Integer page, Integer size);

    /**
     * 获取普通用户会话记录（分页展示）
     * @param page
     * @param size
     * @return
     */
    PageResult<PageSessionMessageVO> getUserSessions(Integer page, Integer size);

    /**
     * 根据会话消息获取历史对话消息
     * @param sessionId
     * @return
     */
    List<HistoricalMessagesVO> getMessages(Integer sessionId);

    /**
     * 根据会话id删除会话记录
     * @param sessionId
     */
    void deleteSessionById(Integer sessionId);

    /**
     * AI流式对话实现
     * @param userId
     * @param conversationDTO
     * @return
     */
    Flux<String> streamChat(Integer userId, ConversationDTO conversationDTO);
}
