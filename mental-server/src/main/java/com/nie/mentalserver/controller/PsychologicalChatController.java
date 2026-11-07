package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ChatSessionQueryDTO;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import com.nie.mentalserver.service.PsychologicalChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/psychological-chat")
@Tag(name = "会话管理")
public class PsychologicalChatController {

    @Autowired
    private PsychologicalChatService psychologicalChatService;

    /**
     *开始会话
     * @param consultationSessionStartDTO
     * @return
     */
    @Operation(summary = "创建会话")
    @PostMapping("/session/start")
    public Result<ConsultationSessionStartVO> sessionStart(@Valid @RequestBody ConsultationSessionStartDTO consultationSessionStartDTO){
        ConsultationSessionStartVO consultationSessionStartVO = psychologicalChatService.sessionStart(consultationSessionStartDTO);
        return Result.success(consultationSessionStartVO);
    }

    /**
     * 不同类型用户获取会话记录（分页查询）
     * @param chatSessionQueryDTO
     * @return
     */
    @Operation(summary = "获取会话记录分页查询")
    @GetMapping("/sessions")
    public Result<PageResult<PageSessionMessageVO>> pageSessions(ChatSessionQueryDTO chatSessionQueryDTO){

        //参数适配转换（抹平不同类型用户差异）
        int page = 1;
        int size = 5;

        //获取当前用户类型
        Integer userType = CurrentHolder.getUserType();
        if(Integer.valueOf(1).equals(userType)){
            //普通用户
            page = chatSessionQueryDTO.getPageNum() != null ? chatSessionQueryDTO.getPageNum() : 1;
            size = chatSessionQueryDTO.getPageSize() != null ? chatSessionQueryDTO.getPageSize() : 5;
            return Result.success(psychologicalChatService.getUserSessions(page,size));
        }else if(Integer.valueOf(2).equals(userType)){
            //管理员
            page = chatSessionQueryDTO.getCurrentPage() != null ? chatSessionQueryDTO.getCurrentPage() : 1;
            size = chatSessionQueryDTO.getSize() != null ? chatSessionQueryDTO.getSize() : 5;
            return Result.success(psychologicalChatService.getAdminSessions(page,size));
        }else{
            throw new BusinessException("请先登录");
        }
    }

    /**
     * 根据会话id获取历史消息
     * @param sessionId
     * @return
     */
    @Operation(summary = "获取历史对话")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<HistoricalMessagesVO>> getMessages(@PathVariable("sessionId") Integer sessionId){
        return Result.success(psychologicalChatService.getMessages(sessionId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Object> deleteSession(@PathVariable("sessionId") Integer sessionId){

        psychologicalChatService.deleteSessionById(sessionId);
        return Result.success();
    }
}
