package com.logicalparadigm.helpdesk.service;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.domain.Role;
import com.logicalparadigm.helpdesk.repository.UserRepository;
import com.logicalparadigm.helpdesk.web.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AppUser> findByEmail(String email) {
        return users.findByEmailIgnoreCase(email);
    }

    public AppUser require(String email) {
        return findByEmail(email).orElseThrow(() -> new IllegalStateException("Signed-in user not found"));
    }

    public boolean emailTaken(String email) {
        return users.existsByEmailIgnoreCase(email.trim());
    }

    /** New accounts made on the sign-up page are always employees. */
    @Transactional
    public AppUser register(RegistrationForm form) {
        AppUser user = new AppUser(
                form.getFullName().trim(),
                form.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(form.getPassword()),
                form.getDepartment() == null ? null : form.getDepartment().trim(),
                Role.EMPLOYEE);
        return users.save(user);
    }

    public List<AppUser> allUsers() {
        return users.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void changeRole(Long userId, Role role, AppUser actingAdmin) {
        AppUser target = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (target.getId().equals(actingAdmin.getId()) && role != Role.ADMIN) {
            throw new IllegalArgumentException("You can't remove your own admin access.");
        }
        target.setRole(role);
    }
}
