package com.nie.mentalserver.service;

import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;

public interface PsychologicalChatService {

    /**
     * 创建会话实现
     * @param consultationSessionStartDTO
     * @return
     */
    ConsultationSessionStartVO sessionStart(ConsultationSessionStartDTO consultationSessionStartDTO);
}
