package com.example.keystone.Controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.keystone.Entity.AppUser;
import com.example.keystone.Entity.Role;
import com.example.keystone.Entity.WorkOrder;
import com.example.keystone.Entity.WorkOrderStatus;
import com.example.keystone.Service.AppUserService;
import com.example.keystone.Service.WorkOrderService;

@Controller
public class PageController {
    private final WorkOrderService workOrderService;
    private final AppUserService userService;
    public PageController(WorkOrderService workOrderService, AppUserService userService) { this.workOrderService = workOrderService; this.userService = userService; }

    @GetMapping("/") public String home() { return "redirect:/dashboard"; }
    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        AppUser currentUser = userService.getUserByEmail(authentication.getName());
        List<WorkOrder> orders = workOrderService.getOrdersForUser(currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("total", orders.size());
        model.addAttribute("newCount", count(orders, WorkOrderStatus.NEW));
        model.addAttribute("assignedCount", count(orders, WorkOrderStatus.ASSIGNED));
        model.addAttribute("inProgressCount", count(orders, WorkOrderStatus.IN_PROGRESS));
        model.addAttribute("completedCount", count(orders, WorkOrderStatus.COMPLETED));
        model.addAttribute("closedCount", count(orders, WorkOrderStatus.CLOSED));
        model.addAttribute("openCount", orders.stream().filter(o -> o.getStatus() != WorkOrderStatus.CLOSED && o.getStatus() != WorkOrderStatus.CANCELLED).count());
        return "dashboard";
    }
    private long count(List<WorkOrder> list, WorkOrderStatus status) { return list.stream().filter(o -> o.getStatus() == status).count(); }
}
