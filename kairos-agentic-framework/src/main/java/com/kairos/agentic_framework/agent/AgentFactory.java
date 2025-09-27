package com.kairos.agentic_framework.agent;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kairos.agentic_framework.tools.KairosTool;
import com.kairos.ai_abstraction.service.ChatLanguageModel;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A factory for creating AiAgent instances.
 * This class encapsulates the logic of discovering tools and wiring them into a
 * LangChain4j AiService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentFactory {

    private final ApplicationContext context; // Used to discover @KairosTool beans
    private final ChatLanguageModel chatLanguageModel;

    @Value("${kairos.are.enabled:false}")
    private boolean isAreEnabled;
    
   // @Autowired(required = false)
   // private GuardianAgent guardianAgent;
    /**
     * Creates a new conversational agent with its own dedicated chat memory.
     * The agent is equipped with all tools available in the Spring application context.
     *
     * @return A fully configured AiAgent instance.
     */
    public AiAgent createAgent() {
        // Discover all beans annotated with @KairosTool
        Collection<Object> tools = context.getBeansWithAnnotation(KairosTool.class).values();
        
        // Each agent instance gets its own memory to maintain separate conversations.
        var chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        
        return AiServices.builder(AiAgent.class)
                .chatModel(chatLanguageModel)
                .tools(tools.toArray())
               // .tools(tool)
                .chatMemory(chatMemory)
                .build();
    }
    
    /**
     * Creates a new conversational agent with a specific system prompt and all available tools.
     * @param systemPrompt The system prompt that defines the agent's persona and mission.
     * @return A fully configured AiAgent instance.
     */
    public AiAgent createAgent(String systemPrompt) {
        Collection<Object> tools = context.getBeansWithAnnotation(KairosTool.class).values();
        var chatMemory = MessageWindowChatMemory.withMaxMessages(20);
        Map<ToolSpecification, ToolExecutor> tool = new ToolSpecifications(context).getSpecifications();//context.getBean(AbstractSpecProvider.class).getSpecification();
        
         
       
        
       /* if (isAreEnabled && guardianAgent != null) {
            log.warn("Axiom-Driven Reasoning Engine (ARE) is ENABLED. Building Multi-Agent Council.");

            // 1. Gather all tools for the Investigative Agent.
            //Collection<Object> staticTools = context.getBeansWithAnnotation(KairosTool.class).values();
            //List<Object> allTools = new ArrayList<>(staticTools);
            // We can add dynamically discovered tools here in the future if needed.
            
            // 2. Build the Investigative Agent AiService.
            // It has its own, temporary memory for each investigation.
            InvestigativeAgent investigativeAgent = AiServices.builder(InvestigativeAgent.class)
                    .chatModel(chatLanguageModel)
                    .tools(tool)
                    .tools(tools.toArray())
                    .chatMemoryProvider(session -> MessageWindowChatMemory.withMaxMessages(5))
                    .build();

            // 3. Build the Executive Agent and inject its council members.
            return new AxiomDrivenExecutiveAgent(
                    chatLanguageModel,
                    chatMemory,
                    systemPrompt, // Pass the main "Kaya" persona here
                    investigativeAgent,
                    guardianAgent
            );

        }*/
        
        
        
        return AiServices.builder(AiAgent.class)
                .chatModel(chatLanguageModel)
                .systemMessageProvider((o)->{ return systemPrompt;})
                .tools(tool)
                .tools(tools.toArray())
                .chatMemory(chatMemory)
                .build();
    }
    
     /**
     * Creates a new conversational agent equipped with a specific set of tools.
     *
     * @param tools A list of tool objects to be used by the agent.
     * @return A fully configured AiAgent instance.
     */
    public AiAgent createAgent(Object... tools) {
        var chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        return AiServices.builder(AiAgent.class)
                .chatModel(chatLanguageModel)
                .tools(tools)
                .chatMemory(chatMemory)
                .build();
    }
}