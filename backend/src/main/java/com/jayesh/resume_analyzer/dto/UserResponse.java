package com.jayesh.resume_analyzer.dto;

import com.jayesh.resume_analyzer.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {}