package com.campusbridge.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class LearningPlanDto {

    private boolean aiGenerated;
    private String disclaimer;
    private String summary;
    private List<LearningStepDto> steps = new ArrayList<>();

    public LearningPlanDto() {
    }

    public LearningPlanDto(boolean aiGenerated, String disclaimer, String summary, List<LearningStepDto> steps) {
        this.aiGenerated = aiGenerated;
        this.disclaimer = disclaimer;
        this.summary = summary;
        this.steps = steps != null ? steps : new ArrayList<>();
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<LearningStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<LearningStepDto> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
