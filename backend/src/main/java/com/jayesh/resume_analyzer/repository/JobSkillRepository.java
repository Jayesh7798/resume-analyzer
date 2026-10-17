package com.jayesh.resume_analyzer.repository;

import com.jayesh.resume_analyzer.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    @Query("select js from JobSkill js join fetch js.skill s " +
            "where js.job.id = :jobId " +
            "order by js.mentions desc, s.skillName asc")
    List<JobSkill> findWithSkillByJobId(@Param("jobId") Long jobId);
    @Query("select js from JobSkill js join fetch js.skill s join fetch js.job j " +
            "order by js.mentions desc, s.skillName asc")
    List<JobSkill> findAllWithSkillAndJob();
}