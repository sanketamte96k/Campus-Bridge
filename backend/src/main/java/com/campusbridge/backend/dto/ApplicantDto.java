package com.campusbridge.backend.dto;

public class ApplicantDto {
    private ApplicationDto application;
    private String name;
    private String email;
    private StudentProfileDto profile;

    public ApplicantDto() {
    }

    public ApplicantDto(ApplicationDto application, String name, String email, StudentProfileDto profile) {
        this.application = application;
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    public ApplicationDto getApplication() {
        return application;
    }

    public void setApplication(ApplicationDto application) {
        this.application = application;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StudentProfileDto getProfile() {
        return profile;
    }

    public void setProfile(StudentProfileDto profile) {
        this.profile = profile;
    }
}
