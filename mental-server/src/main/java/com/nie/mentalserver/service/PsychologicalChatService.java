package com.nie.mentalserver.service;

import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.SessionMessageVO;

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
    PageResult<SessionMessageVO> getAdminSessions(Integer page, Integer size);

    /**
     * 获取普通用户会话记录（分页展示）
     * @param page
     * @param size
     * @return
     */
    PageResult<SessionMessageVO> getUserSessions(Integer page, Integer size);
}
