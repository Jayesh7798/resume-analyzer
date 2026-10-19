package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.dto.AnalysisResponse;
import com.jayesh.resume_analyzer.dto.ApplicationResponse;
import com.jayesh.resume_analyzer.dto.CandidateRankingResponse;
import com.jayesh.resume_analyzer.entity.Analysis;
import com.jayesh.resume_analyzer.entity.JobApplication;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.repository.AnalysisRepository;
import com.jayesh.resume_analyzer.repository.JobApplicationRepository;
import com.jayesh.resume_analyzer.repository.JobRepository;
import com.jayesh.resume_analyzer.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final AnalysisRepository analysisRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final MatchService matchService;

    public ApplicationResponse apply(Long jobId, Long resumeId, User candidate) {
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied to this job");
        }

        // checks the resume belongs to this candidate and is analyzed, checks the job exists,
        // runs the matching and saves the analysis
        AnalysisResponse result = matchService.match(jobId, resumeId, candidate);
        Analysis analysis = analysisRepository.findById(result.id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Analysis could not be loaded"));

        JobApplication application = new JobApplication();
        application.setJob(jobRepository.getReferenceById(jobId));
        application.setCandidate(candidate);
        application.setResume(resumeRepository.getReferenceById(resumeId));
        application.setAnalysis(analysis);

        try {
            JobApplication saved = applicationRepository.save(application);
            return new ApplicationResponse(saved.getId(), saved.getCreatedAt(), result);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied to this job");
        }
    }

    public List<CandidateRankingResponse> rankedCandidates(Long jobId, User recruiter) {
        // a recruiter can only see applicants of their own jobs
        jobRepository.findByIdAndRecruiterId(jobId, recruiter.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        List<CandidateRankingResponse> ranking = new ArrayList<>();
        int rank = 1;
        for (JobApplication ap : applicationRepository.findRankedByJobId(jobId)) {
            Analysis a = ap.getAnalysis();
            ranking.add(new CandidateRankingResponse(
                    rank++, ap.getId(),
                    ap.getCandidate().getId(), ap.getCandidate().getName(), ap.getCandidate().getEmail(),
                    ap.getResume().getId(),
                    a.getOverallScore(), a.getSkillScore(), a.getSemanticScore(), a.getFitScore(),
                    splitSkills(a.getMatchedSkills()), splitSkills(a.getMissingSkills()),
                    a.getExplanation(), ap.getCreatedAt()));
        }
        return ranking;
    }

    private List<String> splitSkills(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }
}
