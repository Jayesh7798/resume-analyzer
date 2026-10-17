package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.JobRequest;
import com.jayesh.resume_analyzer.dto.JobResponse;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody JobRequest request,
                              @AuthenticationPrincipal User user) {
        return jobService.create(request, user);
    }

    @GetMapping
    public List<JobResponse> search(@RequestParam(required = false) String keyword) {
        return jobService.search(keyword);
    }

    @GetMapping("/mine")
    public List<JobResponse> mine(@AuthenticationPrincipal User user) {
        return jobService.listForRecruiter(user);
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable Long id) {
        return jobService.getById(id);
    }
}
