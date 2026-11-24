package com.logicalparadigm.helpdesk.web;

import com.logicalparadigm.helpdesk.domain.AppUser;
import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Priority;
import com.logicalparadigm.helpdesk.domain.Ticket;
import com.logicalparadigm.helpdesk.service.TicketService;
import com.logicalparadigm.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** Pages for regular employees: home, new request, my tickets. */
@Controller
public class EmployeeController {

    private final TicketService ticketService;
    private final UserService userService;

    public EmployeeController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        AppUser me = userService.require(principal.getName());
        List<Ticket> mine = ticketService.forEmployee(me);
        model.addAttribute("recentTickets", mine.stream().limit(5).toList());
        model.addAttribute("openCount", mine.stream().filter(t -> t.getStatus().isActive()).count());
        model.addAttribute("doneCount", mine.stream().filter(t -> !t.getStatus().isActive()).count());
        return "dashboard";
    }

    @GetMapping("/tickets")
    public String myTickets(Principal principal, Model model) {
        AppUser me = userService.require(principal.getName());
        model.addAttribute("tickets", ticketService.forEmployee(me));
        return "tickets/list";
    }

    @GetMapping("/tickets/new")
    public String newTicket(@RequestParam(required = false) Category category, Model model) {
        TicketForm form = new TicketForm();
        form.setCategory(category);
        model.addAttribute("form", form);
        model.addAttribute("priorities", Priority.values());
        return "tickets/new";
    }

    @PostMapping("/tickets")
    public String createTicket(@Valid @ModelAttribute("form") TicketForm form, BindingResult result,
                               Principal principal, Model model, RedirectAttributes flash) {
        if (form.getCategory() != null && form.getRequestType() != null
                && !form.getCategory().getRequestTypes().contains(form.getRequestType())) {
            result.rejectValue("requestType", "invalid", "Please pick a request type from the list");
        }
        if (result.hasErrors()) {
            model.addAttribute("priorities", Priority.values());
            return "tickets/new";
        }
        AppUser me = userService.require(principal.getName());
        Ticket t = ticketService.create(form, me);
        flash.addFlashAttribute("success",
                "Request sent! Your ticket number is " + t.getNumber() + ". The "
                        + t.getCategory().getDisplayName() + " team has been notified.");
        return "redirect:/tickets/" + t.getId();
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Long id, Principal principal, Model model) {
        AppUser me = userService.require(principal.getName());
        model.addAttribute("ticket", ticketService.getForEmployee(id, me));
        return "tickets/detail";
    }

    @PostMapping("/tickets/{id}/comments")
    public String comment(@PathVariable Long id, @RequestParam String message,
                          Principal principal, RedirectAttributes flash) {
        if (message == null || message.isBlank()) {
            flash.addFlashAttribute("error", "Your message was empty.");
        } else if (message.length() > 5000) {
            flash.addFlashAttribute("error", "Please keep messages under 5000 characters.");
        } else {
            ticketService.addEmployeeComment(id, userService.require(principal.getName()), message);
            flash.addFlashAttribute("success", "Message added.");
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/close")
    public String close(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        ticketService.closeByEmployee(id, userService.require(principal.getName()));
        flash.addFlashAttribute("success", "Ticket closed. Glad it's sorted out!");
        return "redirect:/tickets/" + id;
    }
}
