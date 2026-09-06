package com.hotel.reservation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Send each user to their module's home page after login
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }
        if (hasRole(authentication, "ROLE_HOUSEKEEPING")) {
            return "redirect:/housekeeping/tasks";
        }
        return "redirect:/login";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
