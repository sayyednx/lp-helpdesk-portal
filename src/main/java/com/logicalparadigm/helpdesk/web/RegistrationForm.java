package com.logicalparadigm.helpdesk.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Please enter your name")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Please enter your work email")
    @Email(message = "That doesn't look like an email address")
    @Size(max = 180)
    private String email;

    @Size(max = 80)
    private String department;

    @NotBlank(message = "Please choose a password")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
    private String password;

    @NotBlank(message = "Please type your password again")
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
