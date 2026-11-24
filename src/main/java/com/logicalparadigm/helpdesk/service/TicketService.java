package com.logicalparadigm.helpdesk.service;

import com.logicalparadigm.helpdesk.domain.*;
import com.logicalparadigm.helpdesk.repository.TicketRepository;
import com.logicalparadigm.helpdesk.web.TicketForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    private static final List<TicketStatus> ACTIVE =
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_ON_EMPLOYEE);

    private final TicketRepository tickets;
    private final RoutingService routing;
    private final NotificationService notifications;

    public TicketService(TicketRepository tickets, RoutingService routing, NotificationService notifications) {
        this.tickets = tickets;
        this.routing = routing;
        this.notifications = notifications;
    }

    /** Save a new ticket, then email it to the right team. */
    @Transactional
    public Ticket create(TicketForm form, AppUser requester) {
        Ticket t = new Ticket(form.getCategory(), form.getRequestType().trim(), form.getSubject().trim(),
                form.getDescription().trim(), form.getPriority(), requester);
        t = tickets.save(t); // save first so it gets a ticket number

        RoutingRule rule = routing.ruleFor(t.getCategory());
        t.setRoutedTo(rule.hasEmails() ? rule.getEmails() : null);
        t.setEmailSent(notifications.notifyTeam(t, rule.emailList()));
        if (rule.isNotifyEmployee()) {
            notifications.confirmToEmployee(t);
        }
        return t;
    }

    public List<Ticket> forEmployee(AppUser user) {
        return tickets.findByRequesterOrderByCreatedAtDesc(user);
    }

    /** Employees can only open their own tickets. */
    public Ticket getForEmployee(Long id, AppUser user) {
        Ticket t = get(id);
        if (!t.getRequester().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return t;
    }

    public Ticket get(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Ticket> search(Category category, TicketStatus status, String q) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return tickets.search(category, status, query);
    }

    @Transactional
    public void addEmployeeComment(Long id, AppUser user, String message) {
        Ticket t = getForEmployee(id, user);
        t.getComments().add(new TicketComment(t, user, message.trim()));
        // If the team was waiting on the employee, put it back in the team's queue.
        if (t.getStatus() == TicketStatus.WAITING_ON_EMPLOYEE) {
            t.setStatus(TicketStatus.IN_PROGRESS);
        }
        t.touch();
    }

    @Transactional
    public void closeByEmployee(Long id, AppUser user) {
        Ticket t = getForEmployee(id, user);
        t.setStatus(TicketStatus.CLOSED);
        t.getComments().add(new TicketComment(t, user, "Closed by employee."));
        t.touch();
    }

    /** Admin changes status/priority and can leave a reply. The employee gets an email. */
    @Transactional
    public void adminUpdate(Long id, AppUser admin, TicketStatus status, Priority priority, String reply) {
        Ticket t = get(id);
        StringBuilder changes = new StringBuilder();

        if (status != null && status != t.getStatus()) {
            changes.append("Status changed to: ").append(status.getLabel()).append("\n");
            t.setStatus(status);
        }
        if (priority != null && priority != t.getPriority()) {
            t.setPriority(priority);
        }
        if (reply != null && !reply.isBlank()) {
            t.getComments().add(new TicketComment(t, admin, reply.trim()));
            changes.append("\nMessage from ").append(admin.getFullName()).append(":\n").append(reply.trim()).append("\n");
        }
        t.touch();

        if (!changes.isEmpty()) {
            notifications.notifyEmployeeOfUpdate(t, changes.toString().trim());
        }
    }

    /** Numbers for the admin dashboard. */
    public Map<String, Long> statusCounts() {
        Map<String, Long> m = new java.util.LinkedHashMap<>();
        for (TicketStatus s : TicketStatus.values()) {
            m.put(s.name(), tickets.countByStatus(s));
        }
        m.put("TOTAL", tickets.count());
        return m;
    }

    public Map<Category, Long> openByCategory() {
        Map<Category, Long> m = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            m.put(c, tickets.countByCategoryAndStatusIn(c, ACTIVE));
        }
        return m;
    }
}
