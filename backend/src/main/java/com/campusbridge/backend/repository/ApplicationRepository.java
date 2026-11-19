package com.campusbridge.backend.repository;

import com.campusbridge.backend.entity.Application;
import com.campusbridge.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByUserId(String userId);

    List<Application> findByUserIdOrderByAppliedAtDesc(String userId);

    List<Application> findByJobId(String jobId);

    List<Application> findByJobIdOrderByAppliedAtDesc(String jobId);

    List<Application> findByJobIdIn(Collection<String> jobIds);

    Optional<Application> findByJobIdAndUserId(String jobId, String userId);

    long countByJobId(String jobId);

    long countByJobIdAndStatus(String jobId, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);
}
