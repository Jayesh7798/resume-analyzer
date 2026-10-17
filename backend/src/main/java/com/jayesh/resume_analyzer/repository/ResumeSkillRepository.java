package com.jayesh.resume_analyzer.repository;

import com.jayesh.resume_analyzer.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {

    @Query("select rs from ResumeSkill rs join fetch rs.skill s " +
            "where rs.resume.id = :resumeId " +
            "order by rs.mentions desc, s.skillName asc")
    List<ResumeSkill> findWithSkillByResumeId(@Param("resumeId") Long resumeId);
}