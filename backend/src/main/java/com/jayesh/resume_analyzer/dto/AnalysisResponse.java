package com.jayesh.resume_analyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResponse(Long id, Long resumeId, Long jobId, String jobTitle, String company,
                               double skillScore, double semanticScore, double fitScore,
                               double overallScore,
                               List<String> matchedSkills, List<String> missingSkills,
                               String explanation, LocalDateTime createdAt) {}