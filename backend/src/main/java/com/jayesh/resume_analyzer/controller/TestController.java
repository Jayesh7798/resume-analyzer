package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.UserResponse;
import com.jayesh.resume_analyzer.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }


}