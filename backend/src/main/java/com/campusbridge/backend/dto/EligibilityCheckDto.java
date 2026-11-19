package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.EligibilityRuleKey;

public class EligibilityCheckDto {
    private EligibilityRuleKey rule;
    private boolean passed;
    private String message;

    public EligibilityCheckDto() {
    }

    public EligibilityCheckDto(EligibilityRuleKey rule, boolean passed, String message) {
        this.rule = rule;
        this.passed = passed;
        this.message = message;
    }

    public EligibilityRuleKey getRule() {
        return rule;
    }

    public void setRule(EligibilityRuleKey rule) {
        this.rule = rule;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
