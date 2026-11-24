package com.logicalparadigm.helpdesk;

import com.logicalparadigm.helpdesk.domain.Category;
import com.logicalparadigm.helpdesk.domain.Ticket;
import com.logicalparadigm.helpdesk.repository.TicketRepository;
import com.logicalparadigm.helpdesk.repository.UserRepository;
import com.logicalparadigm.helpdesk.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HelpdeskFlowTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired TicketRepository tickets;
    @Autowired RoutingService routing;

    @Test
    void homePageLoadsForVisitors() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/admin/login")).andExpect(status().isOk());
        mvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void privatePagesNeedLogin() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }

    @Test
    void firstAdminIsCreatedAndCanLogIn() throws Exception {
        assertThat(users.findByEmailIgnoreCase("admin@logicalparadigm.local")).isPresent();
        mvc.perform(formLogin("/login").user("email", "admin@logicalparadigm.local")
                        .password("password", "ChangeMe123!"))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void employeeCanSignUpLogInAndFileATicket() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("fullName", "Test Employee")
                        .param("email", "Test.Employee@example.com")
                        .param("department", "Engineering")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(redirectedUrl("/login"));

        mvc.perform(formLogin("/login").user("email", "test.employee@example.com")
                        .password("password", "password123"))
                .andExpect(redirectedUrl("/dashboard"));

        mvc.perform(post("/tickets").with(csrf()).with(user("test.employee@example.com").roles("EMPLOYEE"))
                        .param("category", "PAYROLL")
                        .param("requestType", "Paycheck is missing or wrong")
                        .param("subject", "My last paycheck is short")
                        .param("description", "I was paid for 32 hours instead of 40.")
                        .param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection());

        List<Ticket> all = tickets.findAll();
        assertThat(all).anySatisfy(t -> {
            assertThat(t.getCategory()).isEqualTo(Category.PAYROLL);
            assertThat(t.getRoutedTo()).isEqualTo("payroll@logicalparadigm.local");
        });
    }

    @Test
    @WithMockUser(username = "someone@example.com", roles = "EMPLOYEE")
    void employeesCannotOpenAdminPages() throws Exception {
        mvc.perform(get("/admin/routing")).andExpect(status().isForbidden());
    }

    @Test
    void routingRejectsBadEmails() {
        assertThat(routing.normalize("IT@Example.com, help@example.com"))
                .isEqualTo("it@example.com, help@example.com");
        assertThatThrownBy(() -> routing.normalize("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
