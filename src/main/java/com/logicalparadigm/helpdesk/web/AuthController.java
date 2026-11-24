package com.logicalparadigm.helpdesk.web;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Home page, both login pages, and the sign-up page. */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal != null) {
            AppUser me = userService.require(principal.getName());
            return me.isAdmin() ? "redirect:/admin" : "redirect:/dashboard";
        }
        return "index";
    }

    @GetMapping("/login")
    public String employeeLogin(Principal principal) {
        if (principal != null) return "redirect:/";
        return "login";
    }

    @GetMapping("/admin/login")
    public String adminLogin(Principal principal) {
        if (principal != null) return "redirect:/";
        return "admin/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model, Principal principal) {
        if (principal != null) return "redirect:/";
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result,
                           RedirectAttributes flash) {
        if (form.getEmail() != null && !form.getEmail().isBlank() && userService.emailTaken(form.getEmail())) {
            result.rejectValue("email", "taken", "An account with this email already exists. Try signing in.");
        }
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "The two passwords don't match");
        }
        if (result.hasErrors()) {
            return "register";
        }

        AppUser user = userService.register(form);
        flash.addFlashAttribute("success", "Your account is ready! Sign in to file your first request.");
        flash.addFlashAttribute("email", user.getEmail());
        return "redirect:/login";
    }
}
