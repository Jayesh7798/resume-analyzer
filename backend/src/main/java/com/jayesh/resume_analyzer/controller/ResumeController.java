package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.ResumeDetailResponse;
import com.jayesh.resume_analyzer.dto.ResumeResponse;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResumeResponse upload(@RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal User user) {
        return resumeService.upload(file, user);
    }

    @GetMapping("/my")
    public List<ResumeResponse> myResumes(@AuthenticationPrincipal User user) {
        return resumeService.listForUser(user);
    }
    @GetMapping("/{id}")
    public ResumeDetailResponse getResume(@PathVariable Long id,
                                          @AuthenticationPrincipal User user) {
        return resumeService.getForUser(id, user);
    }
}