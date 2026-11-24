package com.logicalparadigm.helpdesk.domain;

public enum TicketStatus {
    OPEN("Open"),
    IN_PROGRESS("In progress"),
    WAITING_ON_EMPLOYEE("Waiting on you"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String label;

    TicketStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    /** CSS-friendly name, e.g. "in-progress". */
    public String getCss() { return name().toLowerCase().replace('_', '-'); }

    public boolean isActive() { return this != RESOLVED && this != CLOSED; }
}
