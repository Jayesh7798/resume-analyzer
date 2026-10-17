package com.jayesh.resume_analyzer.client;

import java.util.List;

public record RankJobInput(Long id, String text, List<String> skills) {}