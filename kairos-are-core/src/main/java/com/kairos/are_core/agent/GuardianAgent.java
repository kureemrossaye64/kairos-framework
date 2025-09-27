package com.kairos.are_core.agent;

import com.kairos.are_core.engine.ConstitutionalGuardian;
import com.kairos.are_core.model.ContradictionStatus;
import org.springframework.stereotype.Component;

/**
 * A programmatic agent interface for the Constitutional Guardian.
 * This is not an AiService, but a simple wrapper for direct method calls.
 */
@Component
public class GuardianAgent {
    private final ConstitutionalGuardian guardian;

    public GuardianAgent(ConstitutionalGuardian guardian) {
        this.guardian = guardian;
    }

    public ContradictionStatus verify(String proposition) {
        return guardian.verify(proposition);
    }
}