package com.example.keystone.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.keystone.Service.AppUserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    private final AppUserService userService;
    public RegistrationController(AppUserService userService) { this.userService = userService; }

    @GetMapping
    public String registerForm(Model model) {
        if (!model.containsAttribute("registration")) model.addAttribute("registration", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String register(@ModelAttribute("registration") RegistrationForm form, RedirectAttributes ra) {
        if (blank(form.name) || blank(form.email) || blank(form.password) || form.password.length() < 6) {
            ra.addFlashAttribute("error", "Please enter all required fields. Password must contain at least 6 characters.");
            ra.addFlashAttribute("registration", form);
            return "redirect:/register";
        }
        try {
            userService.createCustomerAccount(form.name, form.email, form.phone, form.address, form.password);
            ra.addFlashAttribute("success", "Customer account created successfully. Please login to access your customer portal.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", "This email is already registered. Please login or use another email.");
            ra.addFlashAttribute("registration", form);
            return "redirect:/register";
        }
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }

    public static class RegistrationForm {
        private String name, email, phone, address, password;
        public String getName() { return name; } public void setName(String name) { this.name = name; }
        public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
        public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
    }
}
