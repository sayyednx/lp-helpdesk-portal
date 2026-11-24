package com.logicalparadigm.helpdesk.service;

import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.RoutingRule;
import com.logicalparadigm.helpdesk.repository.RoutingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Reads and saves which team email gets which kind of ticket. */
@Service
public class RoutingService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final RoutingRuleRepository rules;

    public RoutingService(RoutingRuleRepository rules) {
        this.rules = rules;
    }

    /** All rules, in the same order as the Category list. */
    public List<RoutingRule> allRules() {
        List<RoutingRule> list = new ArrayList<>();
        for (Category c : Category.values()) {
            list.add(ruleFor(c));
        }
        return list;
    }

    @Transactional
    public RoutingRule ruleFor(Category category) {
        return rules.findByCategory(category)
                .orElseGet(() -> rules.save(new RoutingRule(category, "")));
    }

    /**
     * Cleans up a comma-separated list of emails.
     * Returns the cleaned list, or throws if any address looks wrong.
     */
    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "";
        List<String> bad = new ArrayList<>();
        List<String> good = new ArrayList<>();
        Arrays.stream(raw.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(s -> (EMAIL.matcher(s).matches() ? good : bad).add(s.toLowerCase()));
        if (!bad.isEmpty()) {
            throw new IllegalArgumentException("These don't look like email addresses: " + String.join(", ", bad));
        }
        return String.join(", ", good.stream().distinct().toList());
    }

    @Transactional
    public void update(Category category, String emails, boolean notifyEmployee, String adminEmail) {
        RoutingRule rule = ruleFor(category);
        rule.setEmails(normalize(emails));
        rule.setNotifyEmployee(notifyEmployee);
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setUpdatedBy(adminEmail);
    }
}
