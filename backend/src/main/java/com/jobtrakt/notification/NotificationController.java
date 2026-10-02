package com.jobtrakt.notification;

import com.jobtrakt.user.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    public NotificationPage list(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return notifications.list(user.id(), page, size);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return notifications.markRead(user.id(), id);
    }

    @PatchMapping("/read-all")
    public java.util.Map<String, Long> markAllRead(@AuthenticationPrincipal UserPrincipal user) {
        return java.util.Map.of("updated", notifications.markAllRead(user.id()));
    }
}
