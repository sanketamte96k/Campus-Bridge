package com.campusbridge.backend.dto;

public class RecruiterStatsDto {
    private long totalJobs;
    private long totalApplications;
    private long shortlistedCount;
    private long selectedCount;
    private long rejectedCount;
    private long pendingReviewCount;

    public RecruiterStatsDto() {
    }

    public RecruiterStatsDto(long totalJobs, long totalApplications, long shortlistedCount,
                             long selectedCount, long rejectedCount, long pendingReviewCount) {
        this.totalJobs = totalJobs;
        this.totalApplications = totalApplications;
        this.shortlistedCount = shortlistedCount;
        this.selectedCount = selectedCount;
        this.rejectedCount = rejectedCount;
        this.pendingReviewCount = pendingReviewCount;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(long selectedCount) {
        this.selectedCount = selectedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(long pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }
}
