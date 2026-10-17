package com.jayesh.resume_analyzer.repository;

import com.jayesh.resume_analyzer.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findByResumeIdAndJobId(Long resumeId, Long jobId);

    @Query("select a from Analysis a join fetch a.job " +
            "where a.id = :id and a.resume.user.id = :userId")
    Optional<Analysis> findOwnedById(@Param("id") Long id, @Param("userId") Long userId);
}
