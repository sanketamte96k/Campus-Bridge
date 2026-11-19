package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.entity.StudentProfile;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.enums.Role;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.exception.ResourceNotFoundException;
import com.campusbridge.backend.repository.StudentProfileRepository;
import com.campusbridge.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public StudentProfileService(StudentProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StudentProfileDto getProfileByUserId(String userId) {
        return profileRepository.findByUserId(userId)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public StudentProfileDto saveProfile(String userId, StudentProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("Only students can have an academic profile.");
        }

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudentProfile newProfile = new StudentProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setBranch(dto.getBranch());
        profile.setCgpa(dto.getCgpa());
        profile.setActiveBacklogs(dto.getActiveBacklogs());
        profile.setSkills(dto.getSkills() != null ? new ArrayList<>(dto.getSkills()) : new ArrayList<>());

        StudentProfile saved = profileRepository.save(profile);
        return toDto(saved);
    }

    public StudentProfileDto toDto(StudentProfile profile) {
        if (profile == null) {
            return null;
        }
        return new StudentProfileDto(
                profile.getBranch(),
                profile.getCgpa(),
                profile.getActiveBacklogs(),
                new ArrayList<>(profile.getSkills())
        );
    }
}
