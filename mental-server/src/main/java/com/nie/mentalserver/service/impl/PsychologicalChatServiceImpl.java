package com.nie.mentalserver.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nie.mental.common.enumeration.ConsultationSessionStatus;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.dto.ConversationDTO;
import com.nie.mentalpojo.entity.ConsultationMessage;
import com.nie.mentalpojo.entity.ConsultationSession;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalpojo.vo.HistoricalMessagesVO;
import com.nie.mentalpojo.vo.PageSessionMessageVO;
import com.nie.mentalpojo.vo.SessionEmotionVO;
import com.nie.mentalserver.mapper.PsychologicalChatMapper;
import com.nie.mentalserver.service.PsychologicalChatService;
import com.nie.mentalserver.util.EmotionAnalysisNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PsychologicalChatServiceImpl implements PsychologicalChatService {

    @Autowired
    private PsychologicalChatMapper psychologicalChatMapper;

    @Autowired
    @Qualifier("chatClient")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Value("${spring.ai.deepseek.chat.model:deepseek-chat}")
    private String aiModel;


    //
    private static final int EMOTION_WINDOW = 20;            // 参与分析的用户消息条数
    private static final int EMOTION_MAX_CHARS = 4000;       // 送入模型的文本总长度上限
    private static final int EMOTION_MESSAGE_MAX_CHARS = 500; // 单条消息截断长度

    // 同一会话两次请求几乎同时到达，用会话级锁保证只调用一次模型
    private final ConcurrentHashMap<Integer, Object> emotionLocks = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("emotionChatClient")
    private ChatClient emotionChatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmotionAnalysisNormalizer emotionAnalysisNormalizer;

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

    /**
     * 根据会话id删除会话记录
     * @param sessionId
     */
    @Transactional
    @Override
    public void deleteSessionById(Integer sessionId){
       if(sessionId == null){
           throw new BusinessException("请选择要删除的会话");
       }
       Integer userId = CurrentHolder.getUserId();
       if(userId == null){
           throw new BusinessException("请先登录");
       }
        //删除关联的历史会话记录
        psychologicalChatMapper.deleteMessageBySessionId(sessionId);
        //删除会话
        int row = psychologicalChatMapper.deleteSessionById(sessionId,userId);
        if(row == 0){
           throw new BusinessException("会话不存在");
       }
        //会话删除后同步清理 Redis 中的对话记忆，避免残留上下文
        try {
            chatMemory.clear(String.valueOf(sessionId));
        } catch (Exception e) {
            log.warn("清理对话记忆失败, sessionId={}", sessionId, e);
        }
    }

    /**
     * AI流式对话实现
     * @param userId
     * @param dto
     * @return
     */
    @Override
    public Flux<String> streamChat(Integer userId, ConversationDTO dto){

        Integer sessionId = dto.getSessionId();
        // 1. 归属校验，防止越权访问别人的会话
        if (psychologicalChatMapper.countSessionByIdAndUserId(sessionId, userId) == 0) {
            throw new BusinessException("会话不存在");
        }
        // 2. 用户消息落库
        //    新会话的第一次对话，sessionStart 已经把这条消息写进 MySQL 了，内容相同则跳过，避免重复
        String lastMessageContent = psychologicalChatMapper.getLastMessageContent(sessionId);
        if (!dto.getUserMessage().equals(lastMessageContent)) {
            psychologicalChatMapper.insertMessage(ConsultationMessage.builder()
                    .sessionId(sessionId)
                    .senderType(1)
                    .messageType(1)
                    .content(dto.getUserMessage())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        // 3. 调用 DeepSeek 流式接口
        StringBuilder fullReply = new StringBuilder();
        return chatClient.prompt()
                .user(dto.getUserMessage())
                // 关键：不传 conversationId，所有会话都会用默认记忆，串话
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(sessionId)))
                .stream()
                .content()
                .doOnNext(fullReply::append)
                .doOnError(e -> log.error("AI 流式对话失败, sessionId={}", sessionId, e))
                //正常结束或客户端主动断开时保存已生成内容；异常结束不保存
                .doFinally(signalType -> {
                    if (signalType == SignalType.ON_COMPLETE || signalType == SignalType.CANCEL) {
                        saveAiMessage(sessionId, fullReply.toString());
                    }
                });
    }

    /**
     * 保存 AI 完整回复
     */
    private void saveAiMessage(Integer sessionId, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            psychologicalChatMapper.insertMessage(ConsultationMessage.builder()
                    .sessionId(sessionId)
                    .senderType(2)
                    .messageType(1)
                    .content(content)
                    .aiModel(aiModel)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("保存 AI 回复失败, sessionId={}", sessionId, e);
        }
    }

    /**
     * 会话情绪分析：缓存命中直接返回，没有新的用户消息就不调模型
     * @param userId 当前登录用户
     * @param sessionId 会话 id
     * @return
     */
    @Override
    public SessionEmotionVO analyzeSessionEmotion(Integer userId, Integer sessionId) {

        if (sessionId == null) {
            throw new BusinessException("会话不存在");
        }
        if (psychologicalChatMapper.countSessionByIdAndUserId(sessionId, userId) == 0) {
            throw new BusinessException("会话不存在");
        }

        SessionEmotionVO cached = readEmotionCache(sessionId);
        if (cached != null) {
            return cached;
        }

        Object lock = emotionLocks.computeIfAbsent(sessionId, key -> new Object());
        synchronized (lock) {
            try {
                // 双检：前一个请求可能刚好分析完并写入了缓存
                cached = readEmotionCache(sessionId);
                if (cached != null) {
                    return cached;
                }

                List<String> messages = psychologicalChatMapper.listRecentUserMessages(sessionId, EMOTION_WINDOW);
                if (messages == null || messages.isEmpty()) {
                    return SessionEmotionVO.neutral();
                }

                SessionEmotionVO analyzed = requestEmotion(sessionId, buildTranscript(messages));
                if (analyzed == null) {
                    // 模型调用失败不写缓存，下轮请求还能重试
                    return SessionEmotionVO.neutral();
                }
                writeEmotionCache(sessionId, analyzed);
                return analyzed;
            } finally {
                emotionLocks.remove(sessionId, lock);
            }
        }
    }

    /**
     * 读缓存：最后一条用户消息时间晚于分析时间，视为过期
     */
    private SessionEmotionVO readEmotionCache(Integer sessionId) {
        ConsultationSession cache = psychologicalChatMapper.getEmotionCache(sessionId);
        if (cache == null
                || !StringUtils.hasText(cache.getLastEmotionAnalysis())
                || cache.getLastEmotionUpdatedAt() == null) {
            return null;
        }

        LocalDateTime lastUserMessageTime = psychologicalChatMapper.getLastUserMessageTime(sessionId);
        if (lastUserMessageTime != null && cache.getLastEmotionUpdatedAt().isBefore(lastUserMessageTime)) {
            return null;
        }

        return emotionAnalysisNormalizer.parse(cache.getLastEmotionAnalysis());
    }

    /**
     * 调用模型，失败返回 null
     */
    private SessionEmotionVO requestEmotion(Integer sessionId, String transcript) {
        try {
            SessionEmotionVO vo = emotionChatClient.prompt()
                    .user(transcript)
                    .call()
                    .entity(SessionEmotionVO.class);
            return vo == null ? null : emotionAnalysisNormalizer.normalize(vo);
        } catch (Exception e) {
            log.error("会话情绪分析失败, sessionId={}", sessionId, e);
            return null;
        }
    }

    /**
     * 回写缓存
     */
    private void writeEmotionCache(Integer sessionId, SessionEmotionVO vo) {
        try {
            psychologicalChatMapper.updateEmotionCache(
                    sessionId,
                    objectMapper.writeValueAsString(vo),
                    LocalDateTime.now());
        } catch (Exception e) {
            log.warn("回写情绪分析缓存失败, sessionId={}", sessionId, e);
        }
    }

    /**
     * 拼接送模型的文本：按时间正序，超长时优先保留最近的消息
     */
    private String buildTranscript(List<String> messagesDesc) {
        List<String> lines = new ArrayList<>();
        for (int i = messagesDesc.size() - 1; i >= 0; i--) {
            String content = messagesDesc.get(i);
            if (!StringUtils.hasText(content)) {
                continue;
            }
            String text = content.length() > EMOTION_MESSAGE_MAX_CHARS
                    ? content.substring(0, EMOTION_MESSAGE_MAX_CHARS) + "…"
                    : content;
            lines.add("用户：" + text);
        }

        // 总长度超限时从最早的消息开始丢弃
        int start = lines.size() - 1;
        int total = 0;
        while (start >= 0) {
            int next = total + lines.get(start).length() + 1;
            if (next > EMOTION_MAX_CHARS) {
                break;
            }
            total = next;
            start--;
        }
        if (start < 0) {
            return String.join("\n", lines);
        }
        return String.join("\n", lines.subList(start + 1, lines.size()));
    }
}






















