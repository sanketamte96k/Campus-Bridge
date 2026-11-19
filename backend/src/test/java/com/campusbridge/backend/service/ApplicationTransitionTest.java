package com.campusbridge.backend.service;

import com.campusbridge.backend.enums.ApplicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application State Transition Tests")
class ApplicationTransitionTest {

    private final ApplicationService applicationService = new ApplicationService(null, null, null, null, null, null);

    @Test
    @DisplayName("Should allow valid transitions from APPLIED to SHORTLISTED or REJECTED")
    void shouldAllowTransitionsFromApplied() {
        assertTrue(applicationService.canTransition(ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED));
        assertTrue(applicationService.canTransition(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED));

        assertFalse(applicationService.canTransition(ApplicationStatus.APPLIED, ApplicationStatus.SELECTED));
        assertFalse(applicationService.canTransition(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN));
        assertFalse(applicationService.canTransition(ApplicationStatus.APPLIED, ApplicationStatus.APPLIED));
    }

    @Test
    @DisplayName("Should allow valid transitions from SHORTLISTED to SELECTED or REJECTED")
    void shouldAllowTransitionsFromShortlisted() {
        assertTrue(applicationService.canTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.SELECTED));
        assertTrue(applicationService.canTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));

        assertFalse(applicationService.canTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.APPLIED));
        assertFalse(applicationService.canTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.WITHDRAWN));
    }

    @Test
    @DisplayName("Terminal states (SELECTED, REJECTED, WITHDRAWN) cannot transition anywhere")
    void terminalStatesCannotTransition() {
        for (ApplicationStatus status : ApplicationStatus.values()) {
            assertFalse(applicationService.canTransition(ApplicationStatus.SELECTED, status));
            assertFalse(applicationService.canTransition(ApplicationStatus.REJECTED, status));
            assertFalse(applicationService.canTransition(ApplicationStatus.WITHDRAWN, status));
        }
    }
}
