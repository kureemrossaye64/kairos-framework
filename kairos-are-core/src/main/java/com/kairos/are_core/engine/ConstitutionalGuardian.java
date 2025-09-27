package com.kairos.are_core.engine;

import com.kairos.ai_abstraction.service.ChatLanguageModel;
import com.kairos.are_core.model.ContradictionStatus;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The Constitutional Guardian. This is a specialized, non-conversational agent
 * responsible for executing the core verification function against the
 * Axiomatic Set.
 */
@Service
@Slf4j
public class ConstitutionalGuardian {

	// Internal interface defining the verification agent's capabilities
	interface VerifierAgent {
		String verify(@UserMessage String proposition, @V("axioms") String axioms);
	}

	private final VerifierAgent verifier;
	private final String fullConstitution;

	public ConstitutionalGuardian(AxiomService axiomService, ChatLanguageModel chatLanguageModel) {
		this.fullConstitution = axiomService.getFullConstitution();

		// The Guardian is built with a highly specific, non-conversational system
		// prompt.
		this.verifier = AiServices.builder(VerifierAgent.class).chatModel(chatLanguageModel)
				.systemMessageProvider((o) -> {
					return "You are a Constitutional Guardian AI. Your only purpose is to determine if a given PROPOSITION provided by another AI"
							+ "violates, is corroborated by, or is neutral with respect to a given set of AXIOMS. "
							+ "Analyze the proposition against the axioms and respond with ONLY one of the following three strings: "
							+ "CONTRADICTS_AXIOM, CORROBORATED_BY_AXIOM, or AXIOM_IS_SILENT.";
				})

				.build();
	}

	/**
	 * The core verification function (checkForContradiction).
	 * 
	 * @param proposition The statement to be verified.
	 * @return The ContradictionStatus judgment.
	 */
	public ContradictionStatus verify(String proposition) {
		log.debug("Guardian is verifying proposition: '{}'", proposition);
		String template = "Proposition: " + proposition;
		template = template + "\r\n" + "Axioms: {{axioms}}";
		String rawResponse = verifier.verify(proposition, this.fullConstitution).trim();
		log.debug("Guardian raw response: '{}'", rawResponse);
		try {
			// Safely convert the LLM's string output to our enum
			return ContradictionStatus.valueOf(rawResponse);
		} catch (IllegalArgumentException e) {
			log.warn("Guardian returned an invalid status: '{}'. Defaulting to AXIOM_IS_SILENT.", rawResponse);
			// SIPA Rule: If the check is ambiguous or fails, we treat it as silent
			// (provisional assent).
			return ContradictionStatus.AXIOM_IS_SILENT;
		}
	}
}