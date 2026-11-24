package com.logicalparadigm.helpdesk.config;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds things every page needs (company name, signed-in user, categories). */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AppProperties props;
    private final UserService userService;

    public GlobalModelAdvice(AppProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    @ModelAttribute("companyName")
    public String companyName() {
        return props.getCompanyName();
    }

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    @ModelAttribute("currentUser")
    public AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.findByEmail(auth.getName()).orElse(null);
    }
}
