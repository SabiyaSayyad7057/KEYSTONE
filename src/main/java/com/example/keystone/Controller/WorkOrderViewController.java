package com.example.keystone.Controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.keystone.Entity.*;
import com.example.keystone.Service.AppUserService;
import com.example.keystone.Service.WorkOrderService;

@Controller
@RequestMapping("/work-orders")
public class WorkOrderViewController {
    private final WorkOrderService workOrderService;
    private final AppUserService userService;
    public WorkOrderViewController(WorkOrderService workOrderService, AppUserService userService) { this.workOrderService = workOrderService; this.userService = userService; }

    @GetMapping
    public String list(Model model, Authentication auth) {
        AppUser user = userService.getUserByEmail(auth.getName());
        model.addAttribute("orders", workOrderService.getOrdersForUser(user));
        model.addAttribute("currentUser", user);
        return "workorders/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("customers", workOrderService.getAllCustomers());
        model.addAttribute("sites", workOrderService.getAllSites());
        return "workorders/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public String create(@RequestParam String title, @RequestParam(required=false) String description,
                         @RequestParam Priority priority, @RequestParam Long customerId, @RequestParam Long siteId) {
        workOrderService.createWorkOrder(title, description, priority, customerId, siteId);
        return "redirect:/work-orders";
    }

    @GetMapping("/request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerRequestForm(Model model, Authentication auth) {
        AppUser user = userService.getUserByEmail(auth.getName());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("sites", workOrderService.getSitesForCustomer(user.getCustomer().getId()));
        return "workorders/customer-request";
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerRequest(@RequestParam String title, @RequestParam(required=false) String description,
                                  @RequestParam Priority priority, @RequestParam Long siteId,
                                  Authentication auth) {
        AppUser user = userService.getUserByEmail(auth.getName());
        workOrderService.createCustomerRequest(title, description, priority, siteId, user);
        return "redirect:/work-orders";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        AppUser user = userService.getUserByEmail(auth.getName());
        WorkOrder order = workOrderService.getWorkOrderById(id);
        if (user.getRole() == Role.CUSTOMER && (user.getCustomer() == null || !user.getCustomer().getId().equals(order.getCustomer().getId()))) throw new AccessDeniedException("Not your work order");
        if (user.getRole() == Role.TECHNICIAN && (order.getAssignee() == null || !order.getAssignee().getId().equals(user.getId()))) throw new AccessDeniedException("This work order is not assigned to you");
        model.addAttribute("workOrder", order);
        model.addAttribute("technicians", workOrderService.getTechnicians());
        model.addAttribute("statuses", workOrderService.getAllowedNextStatuses(order, user));
        model.addAttribute("history", workOrderService.getHistory(id));
        model.addAttribute("currentUser", user);
        return "workorders/details";
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public String assign(@PathVariable Long id, @RequestParam Long technicianId, RedirectAttributes redirectAttributes) {
        try {
            workOrderService.assignTechnician(id, technicianId);
            redirectAttributes.addFlashAttribute("success", "Technician assigned and work order saved.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/work-orders/" + id;
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN','MANAGER')")
    public String status(@PathVariable Long id, @RequestParam WorkOrderStatus status,
                         @RequestParam(required=false) String note, Authentication auth,
                         RedirectAttributes redirectAttributes) {
        try {
            WorkOrder saved = workOrderService.changeStatus(id, status, note, auth.getName());
            redirectAttributes.addFlashAttribute("success", "Status updated to " + saved.getStatus() + " and saved successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/work-orders/" + id;
    }
}
