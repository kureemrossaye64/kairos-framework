package com.kairos.ai_abstraction.service.impl;

import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.request.ChatRequest;

public class KairosChatListener implements ChatModelListener {

	@Override
	public void onRequest(ChatModelRequestContext requestContext) {
		ChatRequest request = requestContext.chatRequest();
		List<ChatMessage>  messages = request.messages();
		for(ChatMessage message : messages) {
			if(message.type() == ChatMessageType.AI) {
				AiMessage aiMessage = (AiMessage)message;
				System.out.println(aiMessage.text());
			}
		}
	}

}
