package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.client.AiAnalysis;
import com.jayesh.resume_analyzer.client.AiServiceClient;
import com.jayesh.resume_analyzer.client.ExtractedSkill;
import com.jayesh.resume_analyzer.dto.ResumeDetailResponse;
import com.jayesh.resume_analyzer.dto.ResumeResponse;
import com.jayesh.resume_analyzer.dto.SkillResponse;
import com.jayesh.resume_analyzer.entity.*;
import com.jayesh.resume_analyzer.repository.ResumeRepository;
import com.jayesh.resume_analyzer.repository.ResumeSkillRepository;
import com.jayesh.resume_analyzer.repository.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final AiServiceClient aiServiceClient;
    private final TransactionTemplate tx;
    private final Path uploadDir;

    public ResumeService(ResumeRepository resumeRepository,
                         SkillRepository skillRepository,
                         ResumeSkillRepository resumeSkillRepository,
                         AiServiceClient aiServiceClient,
                         PlatformTransactionManager transactionManager,
                         @Value("${app.upload-dir}") String uploadDir) {
        this.resumeRepository = resumeRepository;
        this.skillRepository = skillRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.aiServiceClient = aiServiceClient;
        this.tx = new TransactionTemplate(transactionManager);
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public ResumeResponse upload(MultipartFile file, User user) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = originalName.toLowerCase();
        String extension;
        if (lower.endsWith(".pdf")) {
            extension = ".pdf";
        } else if (lower.endsWith(".docx")) {
            extension = ".docx";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF or DOCX files are allowed");
        }

        byte[] bytes;
        Path target;
        try {
            bytes = file.getBytes();
            Files.createDirectories(uploadDir);
            target = uploadDir.resolve(UUID.randomUUID() + extension);
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }

        // Call the Python service first; null means it failed
        AiAnalysis analysis = tryAnalyze(bytes, originalName);

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileName(originalName);
        resume.setFilePath(target.toString());
        if (analysis == null) {
            resume.setStatus(ResumeStatus.FAILED);
        } else {
            resume.setExtractedText(analysis.extractedText());
            resume.setStatus(ResumeStatus.ANALYZED);
        }

        // Save the resume and its skills together: both succeed or neither does
        Resume saved = tx.execute(status -> {
            Resume r = resumeRepository.save(resume);
            if (analysis != null) {
                saveSkills(r, analysis.skills());
            }
            return r;
        });

        return toResponse(saved);
    }

    public List<ResumeResponse> listForUser(User user) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ResumeDetailResponse getForUser(Long id, User user) {
        Resume r = resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        List<SkillResponse> skills = resumeSkillRepository.findWithSkillByResumeId(r.getId())
                .stream()
                .map(rs -> new SkillResponse(rs.getSkill().getSkillName(),
                        rs.getSkill().getCategory(), rs.getMentions()))
                .toList();

        return new ResumeDetailResponse(r.getId(), r.getFileName(), r.getStatus(),
                r.getUploadedAt(), r.getExtractedText(), skills);
    }

    private AiAnalysis tryAnalyze(byte[] bytes, String filename) {
        try {
            return aiServiceClient.analyze(bytes, filename);
        } catch (Exception e) {
            log.warn("Analysis failed for {}: {}", filename, e.getMessage());
            return null;
        }
    }

    private void saveSkills(Resume resume, List<ExtractedSkill> extracted) {
        for (ExtractedSkill es : extracted) {
            // reuse the skill row if it already exists, otherwise create it
            Skill skill = skillRepository.findBySkillName(es.name()).orElseGet(() -> {
                Skill s = new Skill();
                s.setSkillName(es.name());
                s.setCategory(es.category());
                return skillRepository.save(s);
            });

            ResumeSkill link = new ResumeSkill();
            link.setResume(resume);
            link.setSkill(skill);
            link.setMentions(es.mentions());
            resumeSkillRepository.save(link);
        }
    }

    private ResumeResponse toResponse(Resume r) {
        return new ResumeResponse(r.getId(), r.getFileName(), r.getStatus(), r.getUploadedAt());
    }
}