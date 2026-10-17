// AuthResponse.java
package com.jayesh.resume_analyzer.dto;

public record AuthResponse(String token, UserResponse user) {}