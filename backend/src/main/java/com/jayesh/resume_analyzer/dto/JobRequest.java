package com.jayesh.resume_analyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String description,
        String location,
        @Min(0) int minExperience
) {}
