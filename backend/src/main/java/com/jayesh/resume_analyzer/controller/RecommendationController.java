package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.RecommendationResponse;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/api/jobs/recommended")
    public List<RecommendationResponse> recommended(@RequestParam Long resumeId,
                                                    @RequestParam(defaultValue = "10") int limit,
                                                    @AuthenticationPrincipal User user) {
        return recommendationService.recommend(resumeId, limit, user);
    }
}