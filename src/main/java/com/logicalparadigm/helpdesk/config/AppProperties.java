package com.logicalparadigm.helpdesk.config;

import com.logicalparadigm.helpdesk.domain.Category;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

/**
 * Settings from application.yml (section "app").
 * Most can be changed with environment variables, see the README.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Company name shown in the header and emails. */
    private String companyName = "Logical Paradigm";

    /** Public address of the site, used for links inside emails. */
    private String baseUrl = "http://localhost:8080";

    private final Admin admin = new Admin();
    private final Mail mail = new Mail();

    /** Starting team emails. Admins can change them later on the Routing page. */
    private Map<Category, String> defaultRouting = new EnumMap<>(Category.class);

    public static class Admin {
        private String name = "Portal Admin";
        private String email = "admin@logicalparadigm.local";
        private String password = "ChangeMe123!";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Mail {
        /** When false, emails are written to the log instead of being sent. */
        private boolean enabled = false;
        private String from = "helpdesk@logicalparadigm.local";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Admin getAdmin() { return admin; }
    public Mail getMail() { return mail; }
    public Map<Category, String> getDefaultRouting() { return defaultRouting; }
    public void setDefaultRouting(Map<Category, String> defaultRouting) { this.defaultRouting = defaultRouting; }
}
