package com.jayesh.resume_analyzer.dto;

import jakarta.validation.constraints.NotNull;

public record MatchRequest(@NotNull Long resumeId) {}