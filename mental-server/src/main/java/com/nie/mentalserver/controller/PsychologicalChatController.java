package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ChatSessionQueryDTO;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.dto.ConversationDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import com.nie.mentalpojo.vo.SessionEmotionVO;
import com.nie.mentalserver.service.PsychologicalChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
@Tag(name = "会话管理")
public class PsychologicalChatController {

    @Autowired
    private PsychologicalChatService psychologicalChatService;
    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * 根据会话id删除会话
     * @param sessionId
     * @return
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Object> deleteSession(@PathVariable("sessionId") Integer sessionId){
        psychologicalChatService.deleteSessionById(sessionId);
        return Result.success();
    }

    /**
     * AI对话
     * @param dto
     * @return
     */
    @Operation(summary = "AI流式对话")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody ConversationDTO dto){

        Integer userId = CurrentHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return psychologicalChatService.streamChat(userId, dto)
                .map(this::contentFrame)
                .concatWith(Flux.just(ServerSentEvent.builder("{}").event("done").build()))
                .onErrorResume(e -> Flux.just(
                        errorFrame(e),
                        ServerSentEvent.builder("{}").event("done").build()));
    }

    /**
     * 组装正文帧：data: {"code":"200","data":{"content":"..."}}
     */
    private ServerSentEvent<String> contentFrame(String chunk) {
        String json = objectMapper.writeValueAsString(
                Map.of("code", "200", "data", Map.of("content", chunk)));
        return ServerSentEvent.builder(json).build();
    }

    /**
     * 组装错误帧：前端读取的是 message 字段
     */
    private ServerSentEvent<String> errorFrame(Throwable e) {
        String json = objectMapper.writeValueAsString(
                Map.of("code", "-1", "message", "AI回复失败，请稍后重试"));
        return ServerSentEvent.builder(json).build();
    }

    /**
     * 会话情绪分析：前端传的是 session_<id>
     * @param sessionId
     * @return
     */
    @Operation(summary = "会话情绪分析")
    @GetMapping("/session/{sessionId}/emotion")
    public Result<SessionEmotionVO> analyzeSessionEmotion(@PathVariable("sessionId") String sessionId) {

        Integer userId = CurrentHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(
                psychologicalChatService.analyzeSessionEmotion(userId, parseSessionId(sessionId)));
    }

    /**
     * 剥掉前端补的 session_ 前缀
     */
    private Integer parseSessionId(String sessionId) {
        String raw = sessionId == null ? "" : sessionId.trim();
        if (raw.startsWith("session_")) {
            raw = raw.substring("session_".length());
        }
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException e) {
            throw new BusinessException("会话不存在");
        }
    }

























}
