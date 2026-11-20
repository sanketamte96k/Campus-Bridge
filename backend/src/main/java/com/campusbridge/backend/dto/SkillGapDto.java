package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.Branch;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SkillGapDto {

    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private BigDecimal cgpaGap;
    private int backlogsOver;
    private boolean branchAllowed;
    private List<Branch> requiredBranches = new ArrayList<>();
    private boolean closeToEligible;

    public SkillGapDto() {
    }

    public SkillGapDto(
            List<String> matchedSkills,
            List<String> missingSkills,
            BigDecimal cgpaGap,
            int backlogsOver,
            boolean branchAllowed,
            List<Branch> requiredBranches,
            boolean closeToEligible
    ) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
        this.cgpaGap = cgpaGap != null ? cgpaGap : BigDecimal.ZERO;
        this.backlogsOver = backlogsOver;
        this.branchAllowed = branchAllowed;
        this.requiredBranches = requiredBranches != null ? requiredBranches : new ArrayList<>();
        this.closeToEligible = closeToEligible;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public BigDecimal getCgpaGap() {
        return cgpaGap;
    }

    public void setCgpaGap(BigDecimal cgpaGap) {
        this.cgpaGap = cgpaGap != null ? cgpaGap : BigDecimal.ZERO;
    }

    public int getBacklogsOver() {
        return backlogsOver;
    }

    public void setBacklogsOver(int backlogsOver) {
        this.backlogsOver = backlogsOver;
    }

    public boolean isBranchAllowed() {
        return branchAllowed;
    }

    public void setBranchAllowed(boolean branchAllowed) {
        this.branchAllowed = branchAllowed;
    }

    public List<Branch> getRequiredBranches() {
        return requiredBranches;
    }

    public void setRequiredBranches(List<Branch> requiredBranches) {
        this.requiredBranches = requiredBranches != null ? requiredBranches : new ArrayList<>();
    }

    public boolean isCloseToEligible() {
        return closeToEligible;
    }

    public void setCloseToEligible(boolean closeToEligible) {
        this.closeToEligible = closeToEligible;
    }
}
