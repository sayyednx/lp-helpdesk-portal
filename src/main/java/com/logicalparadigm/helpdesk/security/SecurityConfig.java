package com.logicalparadigm.helpdesk.security;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Tells Spring Security how to find a user by email. */
    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return email -> {
            AppUser u = users.findByEmailIgnoreCase(email.trim())
                    .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
            return User.withUsername(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(u.getRole().name())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/admin/login", "/register",
                        "/css/**", "/img/**", "/js/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    /**
     * After login: admins go to the admin dashboard, employees go to their dashboard.
     * If someone uses the admin login page without being an admin, they are signed out again.
     */
    private AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            boolean isAdmin = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            boolean usedAdminPage = "admin".equals(req.getParameter("portal"));

            if (usedAdminPage && !isAdmin) {
                new SecurityContextLogoutHandler().logout(req, res, auth);
                res.sendRedirect(req.getContextPath() + "/admin/login?notadmin");
                return;
            }
            res.sendRedirect(req.getContextPath() + (isAdmin ? "/admin" : "/dashboard"));
        };
    }

    /** Send people back to the same login page they came from, with an error. */
    private AuthenticationFailureHandler failureHandler() {
        return (req, res, ex) -> {
            String page = "admin".equals(req.getParameter("portal")) ? "/admin/login" : "/login";
            try {
                res.sendRedirect(req.getContextPath() + page + "?error");
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
