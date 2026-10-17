package com.jayesh.resume_analyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(Long id, String title, String company, String description,
                          String location, int minExperience, LocalDateTime createdAt,
                          List<SkillResponse> skills) {}