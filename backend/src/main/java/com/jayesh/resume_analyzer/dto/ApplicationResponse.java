package com.jayesh.resume_analyzer.dto;

import java.time.LocalDateTime;

public record ApplicationResponse(Long applicationId, LocalDateTime appliedAt, AnalysisResponse analysis) {}