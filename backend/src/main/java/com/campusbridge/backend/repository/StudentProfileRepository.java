package com.campusbridge.backend.repository;

import com.campusbridge.backend.entity.StudentProfile;
import com.campusbridge.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {
    Optional<StudentProfile> findByUserId(String userId);
    Optional<StudentProfile> findByUser(User user);
}
