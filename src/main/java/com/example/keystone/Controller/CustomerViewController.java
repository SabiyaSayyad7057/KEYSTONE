package com.example.keystone.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.keystone.Entity.Customer;
import com.example.keystone.Service.CustomerService;

@Controller
@RequestMapping("/customers")
public class CustomerViewController {

    private final CustomerService customerService;

    public CustomerViewController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Customer List
    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        return "customers/list";
    }

    // Add Customer Form
    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute(
                "customer",
                new Customer()
        );

        return "customers/form";
    }

    // Save Customer
    @PostMapping("/save")
    public String save(
            @ModelAttribute("customer")
            Customer customer) {

        customerService.saveCustomer(customer);

        return "redirect:/customers";
    }

    // Edit Customer
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "customer",
                customerService.getCustomerById(id)
        );

        return "customers/form";
    }

    // Delete Customer
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Customer cannot be deleted because it is linked to sites, work orders, or an account.");
        }
        return "redirect:/customers";
    }
}