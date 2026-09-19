package com.example.keystone.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.keystone.Entity.Role;
import com.example.keystone.Service.AppUserService;

@Controller
@RequestMapping("/users")
public class UserManagementController {

    private final AppUserService userService;

    public UserManagementController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", Role.values());
        return "users";
    }

    @PostMapping("/create")
    public String createStaff(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam Role role,
                              RedirectAttributes redirectAttributes) {
        if (role == Role.CUSTOMER) {
            redirectAttributes.addFlashAttribute("error", "Customers should use public registration.");
            return "redirect:/users";
        }
        try {
            userService.createUser(name.trim(), email.trim().toLowerCase(), password, role);
            redirectAttributes.addFlashAttribute("success", "User created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", "Email is already registered.");
        }
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/users";
    }
}
