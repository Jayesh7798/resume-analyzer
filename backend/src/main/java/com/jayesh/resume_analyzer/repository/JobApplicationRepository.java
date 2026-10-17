package com.jayesh.resume_analyzer.repository;

import com.jayesh.resume_analyzer.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query("select ap from JobApplication ap " +
            "join fetch ap.analysis an " +
            "join fetch ap.candidate c " +
            "join fetch ap.resume r " +
            "where ap.job.id = :jobId " +
            "order by an.overallScore desc, ap.createdAt asc")
    List<JobApplication> findRankedByJobId(@Param("jobId") Long jobId);
}