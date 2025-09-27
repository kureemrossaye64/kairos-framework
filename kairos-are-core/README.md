# KAIROS - Axiom-Driven Reasoning Engine (ARE) Core

The `kairos-are-core` module is a specialized and critical component of the Kairos Framework, providing the **Axiomatic Verification Layer** for AI agents. Its primary responsibility is to ensure that the behavior of AI agents remains aligned with a predefined set of ethical principles or "axioms." This module is the cornerstone of the experimental **Axiom-Driven Reasoning Engine (ARE)**, a multi-agent architecture designed to produce more reliable, safe, and grounded responses.

## Key Features

- **Constitutional AI:** Implements a form of "Constitutional AI," where an agent's proposed actions or statements are checked against a core constitution before being finalized. This serves as a powerful safety and alignment mechanism.
- **Axiomatic Set:** Defines a clear and explicit set of principles (`AxiomService`) that govern the agent's behavior. These axioms cover critical areas such as user safety, inclusivity, privacy, and the verifiability of information.
- **Guardian Agent:** Features a specialized, non-conversational AI agent known as the `ConstitutionalGuardian`. This agent's sole purpose is to receive a proposition from another AI and judge whether it contradicts, is corroborated by, or is neutral with respect to the defined axioms.
- **Separation of Concerns:** The verification logic is cleanly separated from the primary conversational agent. This allows the main agent to focus on being helpful and creative, while the `ConstitutionalGuardian` handles the critical task of verification.
- **Enum-Based Judgments:** The `ConstitutionalGuardian` provides its judgment as a strongly-typed `ContradictionStatus` enum (`CONTRADICTS_AXIOM`, `CORROBORATED_BY_AXIOM`, `AXIOM_IS_SILENT`), which makes the verification result easy to handle programmatically and reduces ambiguity.

## Core Components

- **`AxiomService`:** A service that loads and provides the set of core axioms. In the current implementation, these are hardcoded, but they could be loaded from a formal specification file (e.g., YAML) in a production environment.
- **`ConstitutionalGuardian`:** A specialized agent that uses a highly specific system prompt to evaluate a given proposition against the full constitution provided by the `AxiomService`. It is the workhorse of the verification layer.
- **`ContradictionStatus`:** An enum that represents the possible outcomes of the verification process.

## How It Works

When the Axiom-Driven Reasoning Engine is active, the `AxiomDrivenExecutiveAgent` (from the `kairos-agentic-framework`) first delegates a user's request to an `InvestigativeAgent` to find a provisional answer. This provisional answer is then passed to the `ConstitutionalGuardian` within this module. The Guardian evaluates the answer against the axioms and returns its judgment. If the answer is found to be in violation of an axiom, the executive agent can then formulate a safe, alternative response.

## Dependencies

- **`kairos-ai-abstraction`:** To provide the `ChatLanguageModel` necessary for the `ConstitutionalGuardian` to perform its verification task.
- **LangChain4j:** For the `AiServices` builder used to create the internal `VerifierAgent`.
- **Spring Boot:** For dependency injection.

This module is a key differentiator of the Kairos Framework, providing a sophisticated mechanism for building more responsible and aligned AI systems.