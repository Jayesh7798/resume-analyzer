package com.jayesh.resume_analyzer.dto;

import java.util.List;

public record RecommendationResponse(Long jobId, String title, String company, String location,
                                     double overallScore, double skillScore, double semanticScore,
                                     double fitScore,
                                     List<String> matchedSkills, List<String> missingSkills,
                                     String explanation) {}