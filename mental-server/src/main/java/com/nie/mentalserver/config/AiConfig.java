package com.nie.mentalserver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    //ChatMemory 由 Spring AI 自动配置创建（底层为 RedisChatMemoryRepository），
    //自定义 ChatMemory 会导致 Redis 仓储自动配置退避，故此处不声明
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder.defaultSystem("""
              你是心理健康支持助手"小暖"。
              1. 用中文、简洁、共情地回应；
              2. 不做医学诊断、不推荐药物，不替代专业治疗；
              3. 发现自伤、自杀或伤人风险时，明确建议立即联系身边人、学校心理中心或拨打心理援助热线。
              """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())//记忆顾问
                .build();
    }

    /**
     * 情绪分析专用：不挂记忆顾问，避免污染咨询上下文，也不会写入 chat-memory
     */
    @Bean
    public ChatClient emotionChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                你是情绪分析器。下面会给出某次心理咨询会话中用户最近说过的话（按时间顺序）。
                请分析用户当前的主要情绪状态，严格按 JSON 输出，不要输出 markdown 代码块或任何解释文字。
                字段要求：
                primaryEmotion：主要情绪，一个中文词，如 焦虑、低落、平静；
                emotionScore：情绪强度，0~100 的整数；
                isNegative：是否负面情绪，true 或 false；
                riskLevel：0 正常 / 1 关注 / 2 预警 / 3 危机，出现自伤、自杀、伤人内容时必须为 3；
                riskDescription：风险描述，一句话；
                suggestion：给用户的温暖建议，50 字以内；
                improvementSuggestions：3 条具体可执行的改善建议组成的数组。
                """)
                .build();
    }
}