package com.logicalparadigm.helpdesk.web;

import com.logicalparadigm.helpdesk.domain.*;
import com.logicalparadigm.helpdesk.service.NotificationService;
import com.logicalparadigm.helpdesk.service.RoutingService;
import com.logicalparadigm.helpdesk.service.TicketService;
import com.logicalparadigm.helpdesk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Admin-only pages. Spring Security blocks everyone else from /admin/**. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TicketService ticketService;
    private final RoutingService routingService;
    private final UserService userService;
    private final NotificationService notifications;

    public AdminController(TicketService ticketService, RoutingService routingService,
                           UserService userService, NotificationService notifications) {
        this.ticketService = ticketService;
        this.routingService = routingService;
        this.userService = userService;
        this.notifications = notifications;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("counts", ticketService.statusCounts());
        model.addAttribute("openByCategory", ticketService.openByCategory());
        model.addAttribute("recentTickets", ticketService.search(null, null, null).stream().limit(8).toList());
        model.addAttribute("rules", routingService.allRules());
        model.addAttribute("liveEmail", notifications.isLiveEmail());
        return "admin/dashboard";
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(required = false) Category category,
                          @RequestParam(required = false) TicketStatus status,
                          @RequestParam(required = false) String q,
                          Model model) {
        model.addAttribute("tickets", ticketService.search(category, status, q));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("fCategory", category);
        model.addAttribute("fStatus", status);
        model.addAttribute("q", q);
        return "admin/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String ticket(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", ticketService.get(id));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("priorities", Priority.values());
        return "admin/ticket";
    }

    @PostMapping("/tickets/{id}")
    public String updateTicket(@PathVariable Long id,
                               @RequestParam TicketStatus status,
                               @RequestParam Priority priority,
                               @RequestParam(required = false) String reply,
                               Principal principal, RedirectAttributes flash) {
        if (reply != null && reply.length() > 5000) {
            flash.addFlashAttribute("error", "Please keep replies under 5000 characters.");
            return "redirect:/admin/tickets/" + id;
        }
        ticketService.adminUpdate(id, userService.require(principal.getName()), status, priority, reply);
        flash.addFlashAttribute("success", "Ticket updated. The employee was notified.");
        return "redirect:/admin/tickets/" + id;
    }

    // ---------- Routing: which email gets which kind of ticket ----------

    @GetMapping("/routing")
    public String routing(Model model) {
        model.addAttribute("rules", routingService.allRules());
        model.addAttribute("liveEmail", notifications.isLiveEmail());
        return "admin/routing";
    }

    /**
     * The form sends fields named like "email_IT_SUPPORT" and "notify_IT_SUPPORT".
     * We check every email first, and only save if they are all OK.
     */
    @PostMapping("/routing")
    public String saveRouting(@RequestParam Map<String, String> params, Principal principal,
                              Model model, RedirectAttributes flash) {
        List<String> errors = new ArrayList<>();
        for (Category c : Category.values()) {
            try {
                routingService.normalize(params.get("email_" + c.name()));
            } catch (IllegalArgumentException e) {
                errors.add(c.getDisplayName() + ": " + e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            flash.addFlashAttribute("error", String.join(" | ", errors));
            return "redirect:/admin/routing";
        }
        for (Category c : Category.values()) {
            routingService.update(c, params.get("email_" + c.name()),
                    params.containsKey("notify_" + c.name()), principal.getName());
        }
        flash.addFlashAttribute("success", "Routing saved. New tickets will go to these emails.");
        return "redirect:/admin/routing";
    }

    @PostMapping("/routing/test/{category}")
    public String testRouting(@PathVariable Category category, RedirectAttributes flash) {
        RoutingRule rule = routingService.ruleFor(category);
        if (!rule.hasEmails()) {
            flash.addFlashAttribute("error", "Add an email for " + category.getDisplayName() + " first, then save.");
        } else if (notifications.sendTest(category, rule.emailList())) {
            flash.addFlashAttribute("success", "Test email sent to " + rule.getEmails());
        } else {
            flash.addFlashAttribute("error", "Email is turned off or the mail server failed, so the test was "
                    + "written to the app log instead. See the README to turn on real email.");
        }
        return "redirect:/admin/routing";
    }

    // ---------- Users ----------

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.allUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                             Principal principal, RedirectAttributes flash) {
        try {
            userService.changeRole(id, role, userService.require(principal.getName()));
            flash.addFlashAttribute("success", "Role updated.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
