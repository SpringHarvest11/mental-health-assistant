package com.nie.mentalserver.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nie.mental.common.enumeration.ConsultationSessionStatus;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.entity.ConsultationMessage;
import com.nie.mentalpojo.entity.ConsultationSession;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import com.nie.mentalserver.mapper.PsychologicalChatMapper;
import com.nie.mentalserver.service.PsychologicalChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        Integer userId = CurrentHolder.getUserId();
        if(userId == null){
            throw new BusinessException("当前用户为NULL");
        }
        //组装ConsultationSession实例对象
        ConsultationSession consultationSession = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(consultationSessionStartDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();
        if( !StringUtils.hasText( consultationSession.getSessionTitle())){
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
                .content(consultationSessionStartDTO.getInitialMessage())
                .emotionTag(null)
                .createdAt(LocalDateTime.now())
                .build();
        //向 consultation_message表中插入一条数据
        psychologicalChatMapper.insertMessage(consultationMessage);


        //3.封装 ConsultationSessionStartVO 对象返回
        return ConsultationSessionStartVO.builder()
                    .sessionId(consultationSession.getId())
                    .status(ConsultationSessionStatus.ACTIVE.toString())
                    .build();
    }

    /**
     * 获取管理员会话记录（分页展示）（看全部会话，带用户昵称）
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<PageSessionMessageVO> getAdminSessions(Integer page, Integer size){
        PageHelper.startPage(page,size);
        Page<PageSessionMessageVO> list = (Page<PageSessionMessageVO>) psychologicalChatMapper.listAdminSessions();
        return new PageResult<>(list.getTotal(),list.getResult());
    }

    /**
     * 获取普通用户会话记录（分页展示）（普通用户：只看自己的会话）
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<PageSessionMessageVO> getUserSessions(Integer page, Integer size){
        Integer userId = CurrentHolder.getUserId();
        PageHelper.startPage(page,size);
        Page<PageSessionMessageVO> list = (Page<PageSessionMessageVO>) psychologicalChatMapper.listUserSessions(userId);
        return new PageResult<>(list.getTotal(),list.getResult());
    }

    /**
     * 根据会话消息获取历史对话消息
     * @param sessionId
     * @return
     */
    @Override
    public List<HistoricalMessagesVO> getMessages(Integer sessionId){
        if(sessionId == null){
            throw new BusinessException("没有会话记录");
        }
        Integer userId = CurrentHolder.getUserId();
        if(userId == null){
            throw new BusinessException("登录异常或未登录");
        }
        Integer userType = CurrentHolder.getUserType();
        return psychologicalChatMapper.getMessagesBySessionId(sessionId,userId,userType);
    }
}
