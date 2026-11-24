package com.logicalparadigm.helpdesk.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false, length = 80)
    private String requestType;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id")
    private AppUser requester;

    /** The team email this ticket was sent to when it was created. */
    @Column(length = 180)
    private String routedTo;

    private boolean emailSent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketComment> comments = new ArrayList<>();

    protected Ticket() { }

    public Ticket(Category category, String requestType, String subject, String description,
                  Priority priority, AppUser requester) {
        this.category = category;
        this.requestType = requestType;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.requester = requester;
    }

    /** Friendly ticket number shown to people, e.g. LP-01042. */
    public String getNumber() {
        return id == null ? "LP-NEW" : String.format("LP-%05d", id);
    }

    public void touch() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getRequestType() { return requestType; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public AppUser getRequester() { return requester; }
    public String getRoutedTo() { return routedTo; }
    public void setRoutedTo(String routedTo) { this.routedTo = routedTo; }
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<TicketComment> getComments() { return comments; }
}
