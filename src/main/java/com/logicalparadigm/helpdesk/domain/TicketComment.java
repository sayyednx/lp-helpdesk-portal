package com.logicalparadigm.helpdesk.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected TicketComment() { }

    public TicketComment(Ticket ticket, AppUser author, String message) {
        this.ticket = ticket;
        this.author = author;
        this.message = message;
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public AppUser getAuthor() { return author; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
