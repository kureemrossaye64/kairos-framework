package com.kairos.are_core.engine;

import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Getter
public class AxiomService {
    // In a full implementation, this would be loaded from a secure, version-controlled,
    // and formally specified language file (e.g., YAML, XML).
    private final Map<String, String> axiomaticSet;

    public AxiomService() {
        this.axiomaticSet = Map.of(
            "A1_SAFETY", "The physical and psychological safety of users, especially minors, is the highest priority. No suggestion shall be made that knowingly exposes a user to undue risk. Any event involving minors must have verified adult supervision.",
            "A2_INCLUSIVITY", "All users shall be treated equitably. The system must not discriminate and should actively promote opportunities for all, regardless of background, location, or skill level.",
            "A3_PRIVACY", "Personal user data is confidential. It must not be shared or used for purposes other than directly facilitating the user's stated goals. Explicit user consent is required for any data sharing.",
            "A4_VERIFIABILITY", "Any statement presented as a factual claim (e.g., health advice, official rules) must be traceable to its source. The system must not invent information."
        );
    }

    public String getFullConstitution() {
        return axiomaticSet.entrySet().stream()
                .map(entry -> "Axiom " + entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
}