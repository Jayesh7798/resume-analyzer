package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.AnalysisResponse;
import com.jayesh.resume_analyzer.dto.MatchRequest;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/jobs/{jobId}/match")
    public AnalysisResponse match(@PathVariable Long jobId,
                                  @Valid @RequestBody MatchRequest request,
                                  @AuthenticationPrincipal User user) {
        return matchService.match(jobId, request.resumeId(), user);
    }

    @GetMapping("/analyses/{id}")
    public AnalysisResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return matchService.getForUser(id, user);
    }
}