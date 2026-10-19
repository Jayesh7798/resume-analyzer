package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.client.AiServiceClient;
import com.jayesh.resume_analyzer.client.MatchResult;
import com.jayesh.resume_analyzer.dto.AnalysisResponse;
import com.jayesh.resume_analyzer.entity.*;
import com.jayesh.resume_analyzer.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class MatchService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final AnalysisRepository analysisRepository;
    private final AiServiceClient aiServiceClient;
    private final TransactionTemplate tx;

    public MatchService(ResumeRepository resumeRepository,
                        JobRepository jobRepository,
                        ResumeSkillRepository resumeSkillRepository,
                        JobSkillRepository jobSkillRepository,
                        AnalysisRepository analysisRepository,
                        AiServiceClient aiServiceClient,
                        PlatformTransactionManager transactionManager) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.analysisRepository = analysisRepository;
        this.aiServiceClient = aiServiceClient;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public AnalysisResponse match(Long jobId, Long resumeId, User candidate) {
        // a candidate can only match their own resumes
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, candidate.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        if (resume.getStatus() != ResumeStatus.ANALYZED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This resume was not analyzed successfully. Upload it again.");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        List<String> resumeSkills = resumeSkillRepository.findWithSkillByResumeId(resumeId).stream()
                .map(rs -> rs.getSkill().getSkillName()).toList();
        // ordered by how often the job mentions them, so "missing" comes out most important first
        List<String> jobSkills = jobSkillRepository.findWithSkillByJobId(jobId).stream()
                .map(js -> js.getSkill().getSkillName()).toList();

        MatchResult result;
        try {
            result = aiServiceClient.match(resume.getExtractedText(), job.getDescription(),
                    resumeSkills, jobSkills, job.getMinExperience());
        } catch (Exception e) {
            log.warn("Matching failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Matching service is unavailable. Try again shortly.");
        }

        Analysis saved = tx.execute(status -> {
            Analysis a = analysisRepository.findByResumeIdAndJobId(resumeId, jobId)
                    .orElseGet(Analysis::new);
            a.setResume(resume);
            a.setJob(job);
            a.setSkillScore(result.skillScore());
            a.setSemanticScore(result.semanticScore());
            a.setFitScore(result.fitScore());
            a.setOverallScore(result.overallScore());
            a.setMatchedSkills(String.join(",", result.matchedSkills()));
            a.setMissingSkills(String.join(",", result.missingSkills()));
            a.setExplanation(result.explanation());
            a.setCreatedAt(LocalDateTime.now());
            return analysisRepository.save(a);
        });

        return toResponse(saved, resumeId, job);
    }

    public AnalysisResponse getForUser(Long analysisId, User user) {
        Analysis a = analysisRepository.findOwnedById(analysisId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found"));
        return toResponse(a, a.getResume().getId(), a.getJob());
    }

    private AnalysisResponse toResponse(Analysis a, Long resumeId, Job job) {
        return new AnalysisResponse(a.getId(), resumeId, job.getId(), job.getTitle(), job.getCompany(),
                a.getSkillScore(), a.getSemanticScore(), a.getFitScore(), a.getOverallScore(),
                splitSkills(a.getMatchedSkills()), splitSkills(a.getMissingSkills()),
                a.getExplanation(), a.getCreatedAt());
    }

    private List<String> splitSkills(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }
}