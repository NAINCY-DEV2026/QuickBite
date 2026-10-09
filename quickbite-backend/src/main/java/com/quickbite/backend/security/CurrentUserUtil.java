package com.quickbite.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserUtil {

    // Extracts the authenticated user's ID from the JWT-populated SecurityContext.
    // This replaces the temporary X-User-Id header approach.
    public Long getCurrentUserId() {
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser().getId();
    }

    public String getCurrentUserRole() {
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser().getRole().name();
    }
}