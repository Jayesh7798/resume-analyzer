package com.jayesh.resume_analyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CandidateRankingResponse(int rank, Long applicationId, Long candidateId,
                                       String candidateName, String candidateEmail, Long resumeId,
                                       double overallScore, double skillScore, double textScore,
                                       List<String> matchedSkills, List<String> missingSkills,
                                       String explanation, LocalDateTime appliedAt) {}
