package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.ChatResponse;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.common.redis.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final LlmGatewayService llmGatewayService;
    private final RedisUtil redisUtil;

    private static final String SESSION_KEY_PREFIX = "ai:chat:session:";
    private static final int SESSION_EXPIRE_MINUTES = 30;
    private static final int MAX_HISTORY_MESSAGES = 10;

    private static final String CHAT_SYSTEM_PROMPT = "你是忠济堂中医养生连锁的智能客服小济。" +
            "你精通中医养生知识，熟悉忠济堂的服务项目、预约流程、会员权益等。" +
            "回答要求：1.专业准确 2.亲切友好 3.简洁明了 4.适时推荐相关服务。" +
            "忠济堂主要服务：中医推拿、艾灸、拔罐、刮痧、体质调理、养生茶饮等。" +
            "如果用户的问题超出你的知识范围，请建议用户联系门店获取专业帮助。";

    private static final List<String> DEFAULT_SUGGESTED_QUESTIONS = Arrays.asList(
            "你们有哪些养生项目？",
            "如何预约服务？",
            "体质调理适合什么人？",
            "会员有什么优惠？"
    );

    public ChatResponse chat(String message, String sessionId, Long memberId) {
        // 1. 从Redis获取会话历史
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) redisUtil.get(SESSION_KEY_PREFIX + sessionId);
        if (history == null) {
            history = new ArrayList<>();
        }

        // 2. 构建消息列表
        List<LlmRequest.Message> messages = new ArrayList<>();
        LlmRequest.Message systemMsg = new LlmRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(CHAT_SYSTEM_PROMPT);
        messages.add(systemMsg);

        // 添加历史消息(最近10条)
        int startIdx = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (int i = startIdx; i < history.size(); i++) {
            LlmRequest.Message historyMsg = new LlmRequest.Message();
            historyMsg.setRole(history.get(i).get("role"));
            historyMsg.setContent(history.get(i).get("content"));
            messages.add(historyMsg);
        }

        // 添加当前用户消息
        LlmRequest.Message userMsg = new LlmRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(message);
        messages.add(userMsg);

        // 3. 调用LlmGatewayService
        LlmRequest request = new LlmRequest();
        request.setSceneType("chat");
        request.setMessages(messages);
        request.setTemperature(0.7);

        LlmResponse response = llmGatewayService.chat(request);

        // 4. 保存会话历史到Redis
        Map<String, String> userHistoryItem = new HashMap<>();
        userHistoryItem.put("role", "user");
        userHistoryItem.put("content", message);
        history.add(userHistoryItem);

        Map<String, String> assistantHistoryItem = new HashMap<>();
        assistantHistoryItem.put("role", "assistant");
        assistantHistoryItem.put("content", response.getContent());
        history.add(assistantHistoryItem);

        // 只保留最近20条消息
        if (history.size() > 20) {
            history = history.subList(history.size() - 20, history.size());
        }

        redisUtil.setWithExpire(SESSION_KEY_PREFIX + sessionId, history,
                SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 5. 返回ChatResponse
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setAnswer(response.getContent());
        chatResponse.setSessionId(sessionId);
        chatResponse.setSuggestedQuestions(DEFAULT_SUGGESTED_QUESTIONS);
        return chatResponse;
    }

    public void clearSession(String sessionId) {
        redisUtil.delete(SESSION_KEY_PREFIX + sessionId);
    }
}
