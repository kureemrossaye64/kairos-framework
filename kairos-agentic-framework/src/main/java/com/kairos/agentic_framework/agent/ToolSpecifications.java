package com.kairos.agentic_framework.agent;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.kairos.agentic_framework.conversational_ingestion.ConversationIngestionToolSpec;
import com.kairos.agentic_framework.transactional_chat.TransactionToolSpec;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;

public class ToolSpecifications {
	
	private ApplicationContext ctx;

	public ToolSpecifications(ApplicationContext ctx) {
		super();
		this.ctx = ctx;
	}
	
	
	public Map<ToolSpecification, ToolExecutor> getSpecifications() {
		ConversationIngestionToolSpec citool = new ConversationIngestionToolSpec(ctx);
		Map<ToolSpecification, ToolExecutor> spec = citool.getSpecification();
		Map<ToolSpecification, ToolExecutor> spec2 = new TransactionToolSpec(ctx).getSpecification();
		
		for(ToolSpecification t : spec2.keySet()) {
			spec.put(t, spec2.get(t));
		}
		
		return spec;
		
	}

}
