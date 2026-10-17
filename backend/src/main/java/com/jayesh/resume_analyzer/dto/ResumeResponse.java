package com.jayesh.resume_analyzer.dto;

import com.jayesh.resume_analyzer.entity.ResumeStatus;

import java.time.LocalDateTime;

public record ResumeResponse(Long id, String fileName, ResumeStatus status, LocalDateTime uploadedAt) {}
