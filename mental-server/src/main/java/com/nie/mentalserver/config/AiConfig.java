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
}