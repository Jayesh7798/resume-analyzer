package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.client.AiServiceClient;
import com.jayesh.resume_analyzer.client.ExtractedSkill;
import com.jayesh.resume_analyzer.dto.JobRequest;
import com.jayesh.resume_analyzer.dto.JobResponse;
import com.jayesh.resume_analyzer.dto.SkillResponse;
import com.jayesh.resume_analyzer.entity.Job;
import com.jayesh.resume_analyzer.entity.JobSkill;
import com.jayesh.resume_analyzer.entity.Skill;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.repository.JobRepository;
import com.jayesh.resume_analyzer.repository.JobSkillRepository;
import com.jayesh.resume_analyzer.repository.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;
    private final AiServiceClient aiServiceClient;
    private final TransactionTemplate tx;

    public JobService(JobRepository jobRepository,
                      JobSkillRepository jobSkillRepository,
                      SkillRepository skillRepository,
                      AiServiceClient aiServiceClient,
                      PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.skillRepository = skillRepository;
        this.aiServiceClient = aiServiceClient;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public JobResponse create(JobRequest req, User recruiter) {
        List<ExtractedSkill> extracted = extractSkillsOrFail(req.title() + "\n" + req.description());

        Job saved = tx.execute(status -> {
            Job job = new Job();
            job.setRecruiter(recruiter);
            job.setTitle(req.title());
            job.setCompany(req.company());
            job.setDescription(req.description());
            job.setLocation(req.location());
            job.setMinExperience(req.minExperience());
            Job savedJob = jobRepository.save(job);

            for (ExtractedSkill es : extracted) {
                JobSkill link = new JobSkill();
                link.setJob(savedJob);
                link.setSkill(findOrCreateSkill(es));
                link.setMentions(es.mentions());
                jobSkillRepository.save(link);
            }
            return savedJob;
        });

        return toResponse(saved);
    }

    public List<JobResponse> search(String keyword) {
        List<Job> jobs = (keyword == null || keyword.isBlank())
                ? jobRepository.findAllByOrderByCreatedAtDesc()
                : jobRepository.search(keyword.trim());
        return jobs.stream().map(this::toResponse).toList();
    }

    public List<JobResponse> listForRecruiter(User recruiter) {
        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiter.getId())
                .stream().map(this::toResponse).toList();
    }

    public JobResponse getById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        return toResponse(job);
    }

    private List<ExtractedSkill> extractSkillsOrFail(String text) {
        try {
            return aiServiceClient.extractSkills(text);
        } catch (Exception e) {
            log.warn("Skill extraction failed for job posting: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Skill extraction service is unavailable. Try again shortly.");
        }
    }

    private Skill findOrCreateSkill(ExtractedSkill es) {
        return skillRepository.findBySkillName(es.name()).orElseGet(() -> {
            Skill s = new Skill();
            s.setSkillName(es.name());
            s.setCategory(es.category());
            return skillRepository.save(s);
        });
    }

    private JobResponse toResponse(Job job) {
        List<SkillResponse> skills = jobSkillRepository.findWithSkillByJobId(job.getId())
                .stream()
                .map(js -> new SkillResponse(js.getSkill().getSkillName(),
                        js.getSkill().getCategory(), js.getMentions()))
                .toList();
        return new JobResponse(job.getId(), job.getTitle(), job.getCompany(), job.getDescription(),
                job.getLocation(), job.getMinExperience(), job.getCreatedAt(), skills);
    }
}
