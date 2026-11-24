package com.logicalparadigm.helpdesk.config;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Role;
import com.logicalparadigm.helpdesk.domain.RoutingRule;
import com.logicalparadigm.helpdesk.repository.RoutingRuleRepository;
import com.logicalparadigm.helpdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once at startup:
 *  1. Creates the first admin account if there are no admins yet.
 *  2. Creates a routing rule (team email) for every ticket category that doesn't have one.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final RoutingRuleRepository routingRules;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public DataSeeder(UserRepository users, RoutingRuleRepository routingRules,
                      PasswordEncoder passwordEncoder, AppProperties props) {
        this.users = users;
        this.routingRules = routingRules;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.countByRole(Role.ADMIN) == 0) {
            AppProperties.Admin a = props.getAdmin();
            users.save(new AppUser(a.getName(), a.getEmail().toLowerCase(),
                    passwordEncoder.encode(a.getPassword()), "IT", Role.ADMIN));
            log.warn("Created first admin account: {} (change the password with ADMIN_PASSWORD!)", a.getEmail());
        }

        for (Category c : Category.values()) {
            if (routingRules.findByCategory(c).isEmpty()) {
                String email = props.getDefaultRouting().getOrDefault(c, "");
                routingRules.save(new RoutingRule(c, email));
                log.info("Created routing rule {} -> {}", c, email.isBlank() ? "(not set)" : email);
            }
        }
    }
}
