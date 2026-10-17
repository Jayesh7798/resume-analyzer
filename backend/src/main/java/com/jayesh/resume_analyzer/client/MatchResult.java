package com.jayesh.resume_analyzer.client;

import java.util.List;

public record MatchResult(double skillScore, double textScore, double overallScore,
                          List<String> matchedSkills, List<String> missingSkills,
                          String explanation) {}
