package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.client.AiServiceClient;
import com.jayesh.resume_analyzer.client.RankJobInput;
import com.jayesh.resume_analyzer.client.RankedJob;
import com.jayesh.resume_analyzer.dto.RecommendationResponse;
import com.jayesh.resume_analyzer.entity.Job;
import com.jayesh.resume_analyzer.entity.Resume;
import com.jayesh.resume_analyzer.entity.ResumeStatus;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.repository.JobRepository;
import com.jayesh.resume_analyzer.repository.JobSkillRepository;
import com.jayesh.resume_analyzer.repository.ResumeRepository;
import com.jayesh.resume_analyzer.repository.ResumeSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final AiServiceClient aiServiceClient;

    public List<RecommendationResponse> recommend(Long resumeId, int limit, User candidate) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, candidate.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        if (resume.getStatus() != ResumeStatus.ANALYZED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This resume was not analyzed successfully. Upload it again.");
        }

        List<Job> jobs = jobRepository.findAll();
        if (jobs.isEmpty()) {
            return List.of();
        }

        List<String> resumeSkills = resumeSkillRepository.findWithSkillByResumeId(resumeId).stream()
                .map(rs -> rs.getSkill().getSkillName())
                .toList();

        // one query for every job's skills, grouped by job id (already ordered most-important first)
        Map<Long, List<String>> skillsByJob = jobSkillRepository.findAllWithSkillAndJob().stream()
                .collect(Collectors.groupingBy(
                        js -> js.getJob().getId(),
                        Collectors.mapping(js -> js.getSkill().getSkillName(), Collectors.toList())));

        List<RankJobInput> inputs = jobs.stream()
                .map(j -> new RankJobInput(j.getId(), j.getDescription(),
                        skillsByJob.getOrDefault(j.getId(), List.of()), j.getMinExperience()))
                .toList();

        List<RankedJob> ranked;
        try {
            ranked = aiServiceClient.rank(resume.getExtractedText(), resumeSkills, inputs);
        } catch (Exception e) {
            log.warn("Ranking failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Matching service is unavailable. Try again shortly.");
        }

        Map<Long, Job> jobsById = jobs.stream()
                .collect(Collectors.toMap(Job::getId, Function.identity()));
        int max = Math.max(1, Math.min(limit, 50));

        return ranked.stream()
                .map(rj -> {
                    Job job = jobsById.get(rj.jobId());
                    if (job == null) {
                        return null;
                    }
                    return new RecommendationResponse(job.getId(), job.getTitle(), job.getCompany(),
                            job.getLocation(), rj.overallScore(), rj.skillScore(), rj.semanticScore(), rj.fitScore(),
                            rj.matchedSkills(), rj.missingSkills(), rj.explanation());
                })
                .filter(Objects::nonNull)
                .limit(max)
                .toList();
    }
}