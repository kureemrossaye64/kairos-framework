package com.kairos.agentic_framework.transactional_chat;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;

import com.kairos.agentic_framework.agent.AbstractSpecProvider;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalEntity;

public class TransactionToolSpec extends AbstractSpecProvider {

	public TransactionToolSpec(ApplicationContext ctx) {
		super(ctx);
	}

	@Override
	public String getEnrichedDescription() {
		Reflections reflections = new Reflections("com.kairos");
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(TransactionalEntity.class);
		String dynamicEntityList = entities.stream().map(cls -> {
			TransactionalEntity annotation = cls.getAnnotation(TransactionalEntity.class);
			return String.format("Transaction: '%s' Description: %s\r\n", annotation.name(), annotation.description());
		}).collect(Collectors.joining(", "));

		return "Starts a complex, multi-step conversational process. "
				+ "This provides the AI with the full context of information needed to complete the task. It is crucial that you  'validateAndSetField' only for fields in the context of the transaction. Do not try to assume non existing fields"
				+ "The AI should use this tool for the following transactions abiding to the description and instruction for each transaction "
				+ dynamicEntityList;
	}

	@Override
	public Method getMethodToEnrich() {
		Method[] methods = TransactionalTool.class.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals("startTransaction")) {
				return m;
			}
		}

		return null;
	}

	@Override
	public Class<?> getAnnotatedToolClass() {
		return TransactionalTool.class;
	}

}
