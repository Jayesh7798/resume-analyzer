package com.jayesh.resume_analyzer.client;

import java.util.List;

public record AiAnalysis(String extractedText, List<ExtractedSkill> skills) {}
