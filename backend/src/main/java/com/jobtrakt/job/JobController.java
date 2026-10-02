package com.jobtrakt.job;

import com.jobtrakt.common.PageResponse;
import com.jobtrakt.user.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/jobs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {
    private final JobService jobs;
    public JobController(JobService jobs) { this.jobs = jobs; }

    @GetMapping
    public PageResponse<JobResponse> list(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return jobs.list(user.id(), status, page, size);
    }

    @GetMapping("/{id}")
    public JobResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return jobs.get(user.id(), id);
    }

    @PostMapping("/{id}/retry")
    public JobResponse retry(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return jobs.retry(user.id(), id);
    }
}
