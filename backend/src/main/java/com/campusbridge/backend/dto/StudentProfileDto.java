package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.Branch;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class StudentProfileDto {

    @NotNull(message = "Branch is required")
    private Branch branch;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "CGPA must be at most 10.0")
    private Double cgpa;

    @NotNull(message = "Active backlogs is required")
    @Min(value = 0, message = "Active backlogs cannot be negative")
    private Integer activeBacklogs;

    @NotNull(message = "Skills list is required")
    private List<String> skills = new ArrayList<>();

    public StudentProfileDto() {
    }

    public StudentProfileDto(Branch branch, Double cgpa, Integer activeBacklogs, List<String> skills) {
        this.branch = branch;
        this.cgpa = cgpa;
        this.activeBacklogs = activeBacklogs;
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(Integer activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }
}
