package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.ApplicationResponse;
import com.jayesh.resume_analyzer.dto.CandidateRankingResponse;
import com.jayesh.resume_analyzer.dto.MatchRequest;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@PathVariable Long jobId,
                                     @Valid @RequestBody MatchRequest request,
                                     @AuthenticationPrincipal User user) {
        return applicationService.apply(jobId, request.resumeId(), user);
    }

    @GetMapping("/{jobId}/candidates")
    public List<CandidateRankingResponse> candidates(@PathVariable Long jobId,
                                                     @AuthenticationPrincipal User user) {
        return applicationService.rankedCandidates(jobId, user);
    }
}