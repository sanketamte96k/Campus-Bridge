package com.campusbridge.backend.repository;

import com.campusbridge.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByPostedBy(String postedBy);

    List<Job> findByPostedByOrderByCreatedAtDesc(String postedBy);

    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Job> searchJobs(@Param("query") String query);

    List<Job> findAllByOrderByCreatedAtDesc();
}
