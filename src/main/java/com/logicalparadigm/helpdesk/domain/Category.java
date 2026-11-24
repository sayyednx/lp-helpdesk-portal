package com.logicalparadigm.helpdesk.domain;

import java.util.List;

/**
 * The kinds of help an employee can ask for.
 * Each category is routed to one team email, which admins set on the Routing page.
 */
public enum Category {

    IT_SUPPORT("IT Support", "laptop",
            "Laptop, password, software, Wi-Fi or VPN problems",
            List.of("Laptop or computer problem", "Password reset / locked out", "New software or license",
                    "Wi-Fi, VPN or network issue", "Email or Microsoft 365", "Other IT request")),

    HR("Human Resources", "people",
            "PTO, benefits, onboarding and workplace questions",
            List.of("Time off (PTO) question", "Benefits question", "Onboarding / offboarding",
                    "Update my personal info", "Workplace concern", "Other HR request")),

    PAYROLL("Payroll", "money",
            "Paycheck, direct deposit and tax form questions",
            List.of("Paycheck is missing or wrong", "Change direct deposit", "W-2 or tax form",
                    "Overtime or hours question", "Reimbursement", "Other payroll request")),

    IMMIGRATION("Immigration", "passport",
            "Visa, work authorization and document requests",
            List.of("Employment verification letter", "Visa / H-1B question", "Green card process",
                    "I-9 or work authorization", "Travel document request", "Other immigration request"));

    private final String displayName;
    private final String icon;
    private final String description;
    private final List<String> requestTypes;

    Category(String displayName, String icon, String description, List<String> requestTypes) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.requestTypes = requestTypes;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public List<String> getRequestTypes() { return requestTypes; }
}
