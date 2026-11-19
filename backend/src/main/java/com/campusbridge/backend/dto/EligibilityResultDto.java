package com.campusbridge.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class EligibilityResultDto {
    private boolean eligible;
    private List<EligibilityCheckDto> checks = new ArrayList<>();

    public EligibilityResultDto() {
    }

    public EligibilityResultDto(boolean eligible, List<EligibilityCheckDto> checks) {
        this.eligible = eligible;
        this.checks = checks != null ? checks : new ArrayList<>();
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<EligibilityCheckDto> getChecks() {
        return checks;
    }

    public void setChecks(List<EligibilityCheckDto> checks) {
        this.checks = checks != null ? checks : new ArrayList<>();
    }
}
