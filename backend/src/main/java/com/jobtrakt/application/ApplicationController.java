package com.jobtrakt.application;

import com.jobtrakt.common.PageResponse;
import com.jobtrakt.user.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/applications")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {
    private final ApplicationService applications;
    public ApplicationController(ApplicationService applications) { this.applications = applications; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody ApplicationRequest request) {
        return applications.create(user.id(), request);
    }

    @GetMapping
    public PageResponse<ApplicationResponse> list(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return applications.list(user.id(), status, search, page, size, sort);
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return applications.get(user.id(), id);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id,
                                      @Valid @RequestBody ApplicationRequest request) {
        return applications.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        applications.delete(user.id(), id);
    }
}
