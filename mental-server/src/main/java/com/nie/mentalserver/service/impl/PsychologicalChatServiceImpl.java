package com.nie.mentalserver.service.impl;

import com.nie.mental.common.enumeration.ConsultationSessionStatus;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.entity.ConsultationMessage;
import com.nie.mentalpojo.entity.ConsultationSession;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalserver.mapper.PsychologicalChatMapper;
import com.nie.mentalserver.service.PsychologicalChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PsychologicalChatServiceImpl implements PsychologicalChatService {

    @Autowired
    private PsychologicalChatMapper psychologicalChatMapper;

    /**
     * 创建会话实现（开始第一条记录就是创建一个新的会话）
     * @param consultationSessionStartDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSessionStartVO sessionStart(ConsultationSessionStartDTO consultationSessionStartDTO){

        //1.向 consultation_session表中插入数据
        //获取当前登录用户id
        Integer userId = CurrentHolder.getCurrentId();
        if(userId == null){
            throw new BusinessException("当前用户为NULL");
        }
        //组装ConsultationSession实例对象
        ConsultationSession consultationSession = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(consultationSessionStartDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();
        if(StringUtils.hasText( consultationSession.getSessionTitle())){
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            consultationSession.setSessionTitle(String.format("小暖AI助手-" + LocalDateTime.now().format(fmt)));
        }
        //向consultation_session表中插入一条数据
        psychologicalChatMapper.insertSession(consultationSession);

        //2.向 consultation_message表中插入数据
        //组装 ConsultationMessage实例对象
        ConsultationMessage consultationMessage = ConsultationMessage.builder()
                .sessionId(consultationSession.getId())
                .senderType(1)
                .messageType(1)
                .context(consultationSessionStartDTO.getInitialMessage())
                .emotionTag(null)
                .createdAt(LocalDateTime.now())
                .build();
        //向 consultation_message表中插入一条数据
        psychologicalChatMapper.insertMessage(consultationMessage);


        //3.封装 ConsultationSessionStartVO 对象返回
        String sessionId = "session_" + consultationSession.getId();
        return ConsultationSessionStartVO.builder()
                    .sessionId(sessionId)
                    .status(ConsultationSessionStatus.ACTIVE.toString())
                    .build();
    }
}
