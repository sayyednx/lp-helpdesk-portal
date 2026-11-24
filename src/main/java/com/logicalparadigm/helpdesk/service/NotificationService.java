package com.logicalparadigm.helpdesk.service;

import com.logicalparadigm.helpdesk.config.AppProperties;
import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Sends emails. If email is turned off (app.mail.enabled=false) or no mail server is set up,
 * the email is printed to the log instead, so the app still works on your laptop.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final ObjectProvider<JavaMailSender> mailSender;
    private final AppProperties props;

    public NotificationService(ObjectProvider<JavaMailSender> mailSender, AppProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    /** Email the team that owns this category. Returns true if the email went out. */
    public boolean notifyTeam(Ticket t, String[] teamEmails) {
        if (teamEmails.length == 0) {
            log.warn("No routing email set for {}. Ticket {} was saved but no one was emailed.",
                    t.getCategory(), t.getNumber());
            return false;
        }
        String subject = "[%s] New %s request: %s".formatted(
                t.getNumber(), t.getCategory().getDisplayName(), t.getSubject());
        String body = """
                A new request was submitted on the %s Help Center.

                Ticket:      %s
                Category:    %s
                Type:        %s
                Priority:    %s
                From:        %s <%s>%s
                Submitted:   %s

                ----- Details -----
                %s
                -------------------

                Open the ticket: %s/admin/tickets/%d
                """.formatted(
                props.getCompanyName(), t.getNumber(), t.getCategory().getDisplayName(), t.getRequestType(),
                t.getPriority().getLabel(), t.getRequester().getFullName(), t.getRequester().getEmail(),
                t.getRequester().getDepartment() == null || t.getRequester().getDepartment().isBlank()
                        ? "" : " (" + t.getRequester().getDepartment() + ")",
                t.getCreatedAt().format(WHEN), t.getDescription(), props.getBaseUrl(), t.getId());
        return send(teamEmails, subject, body, t.getRequester().getEmail());
    }

    /** Let the employee know we got their request. */
    public void confirmToEmployee(Ticket t) {
        String subject = "[%s] We got your request: %s".formatted(t.getNumber(), t.getSubject());
        String body = """
                Hi %s,

                Thanks! Your %s request was received. Your ticket number is %s.
                The team will get back to you soon.

                See your ticket any time: %s/tickets/%d

                - %s Help Center
                """.formatted(firstName(t), t.getCategory().getDisplayName(), t.getNumber(),
                props.getBaseUrl(), t.getId(), props.getCompanyName());
        send(new String[]{t.getRequester().getEmail()}, subject, body, null);
    }

    /** Tell the employee their ticket changed (new status or a reply from the team). */
    public void notifyEmployeeOfUpdate(Ticket t, String whatChanged) {
        String subject = "[%s] Update on your request: %s".formatted(t.getNumber(), t.getSubject());
        String body = """
                Hi %s,

                There is an update on your ticket %s.

                %s

                Current status: %s
                See the full ticket: %s/tickets/%d

                - %s Help Center
                """.formatted(firstName(t), t.getNumber(), whatChanged, t.getStatus().getLabel(),
                props.getBaseUrl(), t.getId(), props.getCompanyName());
        send(new String[]{t.getRequester().getEmail()}, subject, body, null);
    }

    /** Used by the "Send test email" button on the Routing page. */
    public boolean sendTest(Category category, String[] emails) {
        String subject = "Test: %s tickets will come to this inbox".formatted(category.getDisplayName());
        String body = """
                This is a test from the %s Help Center.

                New %s tickets will be emailed to this address.
                """.formatted(props.getCompanyName(), category.getDisplayName());
        return send(emails, subject, body, null);
    }

    public boolean isLiveEmail() {
        return props.getMail().isEnabled() && mailSender.getIfAvailable() != null;
    }

    private boolean send(String[] to, String subject, String body, String replyTo) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!props.getMail().isEnabled() || sender == null) {
            log.info("""

                    ===== EMAIL (not sent: email is turned off) =====
                    To: {}
                    Subject: {}

                    {}
                    =================================================""", String.join(", ", to), subject, body);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(props.getMail().getFrom());
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            if (replyTo != null) msg.setReplyTo(replyTo);
            sender.send(msg);
            log.info("Email sent to {}: {}", String.join(", ", to), subject);
            return true;
        } catch (Exception e) {
            // A broken mail server should never stop someone from filing a ticket.
            log.error("Could not send email to {}: {}", String.join(", ", to), e.getMessage());
            return false;
        }
    }

    private static String firstName(Ticket t) {
        return t.getRequester().getFullName().trim().split("\\s+")[0];
    }
}
