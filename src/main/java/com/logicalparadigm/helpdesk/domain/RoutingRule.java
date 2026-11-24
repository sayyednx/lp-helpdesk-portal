package com.logicalparadigm.helpdesk.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Says which email address gets new tickets for a category.
 * Admins edit these on the Admin > Routing page.
 */
@Entity
@Table(name = "routing_rules")
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private Category category;

    /** One or more emails, separated by commas. */
    @Column(length = 500)
    private String emails;

    /** If true, the employee also gets a copy of the confirmation email. */
    private boolean notifyEmployee = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(length = 180)
    private String updatedBy;

    protected RoutingRule() { }

    public RoutingRule(Category category, String emails) {
        this.category = category;
        this.emails = emails;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getEmails() { return emails; }
    public void setEmails(String emails) { this.emails = emails; }
    public boolean isNotifyEmployee() { return notifyEmployee; }
    public void setNotifyEmployee(boolean notifyEmployee) { this.notifyEmployee = notifyEmployee; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public boolean hasEmails() { return emails != null && !emails.isBlank(); }

    public String[] emailList() {
        if (!hasEmails()) return new String[0];
        return java.util.Arrays.stream(emails.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
