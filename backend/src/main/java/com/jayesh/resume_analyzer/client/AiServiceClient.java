package com.jayesh.resume_analyzer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(@Value("${ai.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public AiAnalysis analyze(byte[] fileBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        Map<String, Object> response = restClient.post()
                .uri("/analyze-resume")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || response.get("extracted_text") == null) {
            throw new IllegalStateException("AI service returned no text");
        }

        String text = response.get("extracted_text").toString();
        return new AiAnalysis(text, parseSkills(response.get("skills")));
    }

    public List<ExtractedSkill> extractSkills(String text) {
        Map<String, Object> response = restClient.post()
                .uri("/extract-skills")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null) {
            throw new IllegalStateException("AI service returned no response");
        }
        return parseSkills(response.get("skills"));
    }

    public MatchResult match(String resumeText, String jobText, List<String> resumeSkills,
                             List<String> jobSkills, int minExperience) {
        Map<String, Object> body = Map.of(
                "resume_text", resumeText,
                "job_text", jobText,
                "resume_skills", resumeSkills,
                "job_skills", jobSkills,
                "min_experience", minExperience);

        Map<String, Object> r = restClient.post()
                .uri("/match")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (r == null) {
            throw new IllegalStateException("AI service returned no response");
        }
        return new MatchResult(
                toDouble(r.get("skill_score")),
                toDouble(r.get("semantic_score")),
                toDouble(r.get("fit_score")),
                toDouble(r.get("overall_score")),
                toStringList(r.get("matched_skills")),
                toStringList(r.get("missing_skills")),
                r.get("explanation") == null ? "" : r.get("explanation").toString());
    }

    public List<RankedJob> rank(String resumeText, List<String> resumeSkills,
                                List<RankJobInput> jobs) {
        List<Map<String, Object>> jobMaps = jobs.stream()
                .map(j -> Map.<String, Object>of(
                        "id", j.id(),
                        "text", j.text(),
                        "skills", j.skills(),
                        "min_experience", j.minExperience()))
                .toList();

        Map<String, Object> body = Map.of(
                "resume_text", resumeText,
                "resume_skills", resumeSkills,
                "jobs", jobMaps);

        Map<String, Object> r = restClient.post()
                .uri("/rank")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (r == null) {
            throw new IllegalStateException("AI service returned no response");
        }

        List<RankedJob> ranked = new ArrayList<>();
        if (r.get("results") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    ranked.add(new RankedJob(
                            m.get("job_id") instanceof Number id ? id.longValue() : -1L,
                            toDouble(m.get("skill_score")),
                            toDouble(m.get("semantic_score")),
                            toDouble(m.get("fit_score")),
                            toDouble(m.get("overall_score")),
                            toStringList(m.get("matched_skills")),
                            toStringList(m.get("missing_skills")),
                            m.get("explanation") == null ? "" : m.get("explanation").toString()));
                }
            }
        }
        return ranked;
    }

    private List<ExtractedSkill> parseSkills(Object raw) {
        List<ExtractedSkill> skills = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map && map.get("name") != null) {
                    String name = map.get("name").toString();
                    String category = map.get("category") == null ? null : map.get("category").toString();
                    int mentions = map.get("mentions") instanceof Number n ? n.intValue() : 1;
                    skills.add(new ExtractedSkill(name, category, mentions));
                }
            }
        }
        return skills;
    }

    private double toDouble(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0.0;
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}