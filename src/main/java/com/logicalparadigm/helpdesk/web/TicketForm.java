package com.logicalparadigm.helpdesk.web;

import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketForm {

    @NotNull(message = "Please pick a category")
    private Category category;

    @NotBlank(message = "Please pick what kind of request this is")
    @Size(max = 80)
    private String requestType;

    @NotBlank(message = "Please add a short title")
    @Size(max = 150, message = "Keep the title under 150 characters")
    private String subject;

    @NotBlank(message = "Please tell us what's going on")
    @Size(max = 5000, message = "Please keep it under 5000 characters")
    private String description;

    @NotNull
    private Priority priority = Priority.MEDIUM;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}
