package com.campusbridge.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class LearningStepDto {

    private String skill;
    private String whyItMatters;
    private List<String> practiceIdeas = new ArrayList<>();
    private int estimatedHours;

    public LearningStepDto() {
    }

    public LearningStepDto(String skill, String whyItMatters, List<String> practiceIdeas, int estimatedHours) {
        this.skill = skill;
        this.whyItMatters = whyItMatters;
        this.practiceIdeas = practiceIdeas != null ? practiceIdeas : new ArrayList<>();
        this.estimatedHours = estimatedHours;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public void setWhyItMatters(String whyItMatters) {
        this.whyItMatters = whyItMatters;
    }

    public List<String> getPracticeIdeas() {
        return practiceIdeas;
    }

    public void setPracticeIdeas(List<String> practiceIdeas) {
        this.practiceIdeas = practiceIdeas != null ? practiceIdeas : new ArrayList<>();
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
}
