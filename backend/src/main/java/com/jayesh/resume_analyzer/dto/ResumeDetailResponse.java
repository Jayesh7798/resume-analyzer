package com.jayesh.resume_analyzer.dto;

import com.jayesh.resume_analyzer.entity.ResumeStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeDetailResponse(Long id, String fileName, ResumeStatus status,
                                   LocalDateTime uploadedAt, String extractedText,
                                   List<SkillResponse> skills) {}