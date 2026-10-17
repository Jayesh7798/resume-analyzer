package com.jayesh.resume_analyzer.repository;

import com.jayesh.resume_analyzer.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByIdAndRecruiterId(Long id, Long recruiterId);

    List<Job> findAllByOrderByCreatedAtDesc();

    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);

    @Query("select j from Job j " +
            "where lower(j.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(j.company) like lower(concat('%', :keyword, '%')) " +
            "or lower(j.description) like lower(concat('%', :keyword, '%')) " +
            "order by j.createdAt desc")
    List<Job> search(@Param("keyword") String keyword);
}
