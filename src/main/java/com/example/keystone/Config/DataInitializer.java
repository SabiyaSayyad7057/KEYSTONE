package com.example.keystone.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.example.keystone.Entity.*;
import com.example.keystone.Repo.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(AppUserRepository users, CustomerRepository customers, SiteRepository sites,
                                WorkOrderRepository orders, PasswordEncoder encoder) {
        return args -> seed(users, customers, sites, orders, encoder);
    }

    @Transactional
    void seed(AppUserRepository users, CustomerRepository customers, SiteRepository sites,
              WorkOrderRepository orders, PasswordEncoder encoder) {
        AppUser manager = user(users, encoder, "Keystone Manager", "manager@keystone.com", "manager123", Role.MANAGER);
        AppUser dispatcher = user(users, encoder, "Dispatch Coordinator", "dispatcher@keystone.com", "dispatcher123", Role.DISPATCHER);
        AppUser technician = user(users, encoder, "Arjun Technician", "technician@keystone.com", "tech123", Role.TECHNICIAN);

        Customer customer = customers.findByEmail("customer@keystone.com").orElseGet(() -> {
            Customer c = new Customer(); c.setName("Acme Manufacturing Pvt. Ltd."); c.setEmail("customer@keystone.com");
            c.setPhone("9876543210"); c.setAddress("Andheri East, Mumbai, Maharashtra"); return customers.save(c);
        });
        AppUser customerUser = user(users, encoder, "Acme Service Coordinator", "customer@keystone.com", "customer123", Role.CUSTOMER);
        if (customerUser.getCustomer() == null) { customerUser.setCustomer(customer); users.save(customerUser); }

        Site site = sites.findAll().stream().filter(s -> "Acme Andheri Plant".equals(s.getName())).findFirst().orElseGet(() -> {
            Site s = new Site(); s.setName("Acme Andheri Plant"); s.setAddress("MIDC Andheri East, Mumbai"); s.setCustomer(customer); return sites.save(s);
        });
        Site site2 = sites.findAll().stream().filter(s -> "Acme Bhiwandi Warehouse".equals(s.getName())).findFirst().orElseGet(() -> {
            Site s = new Site(); s.setName("Acme Bhiwandi Warehouse"); s.setAddress("Bhiwandi Industrial Area, Thane"); s.setCustomer(customer); return sites.save(s);
        });

        if (orders.count() == 0) {
            WorkOrder o1 = order(orders, "Air Conditioning Failure", "Production floor AC unit is not cooling.", Priority.HIGH, WorkOrderStatus.ASSIGNED, customer, site, technician);
            WorkOrder o2 = order(orders, "Electrical Panel Inspection", "Quarterly inspection and thermal scan required.", Priority.MEDIUM, WorkOrderStatus.IN_PROGRESS, customer, site2, technician);
            order(orders, "Emergency Water Leakage", "Leak reported near the loading bay.", Priority.CRITICAL, WorkOrderStatus.NEW, customer, site2, null);
        }
    }

    private AppUser user(AppUserRepository repo, PasswordEncoder enc, String name, String email, String password, Role role) {
        return repo.findByEmail(email).orElseGet(() -> {
            AppUser u = new AppUser(); u.setName(name); u.setEmail(email); u.setPasswordHash(enc.encode(password)); u.setRole(role); return repo.save(u);
        });
    }

    private WorkOrder order(WorkOrderRepository repo, String title, String desc, Priority p, WorkOrderStatus status,
                            Customer customer, Site site, AppUser assignee) {
        WorkOrder o = new WorkOrder(); o.setCode("WO-DEMO-" + Math.abs(title.hashCode())); o.setTitle(title); o.setDescription(desc);
        o.setPriority(p); o.setStatus(status); o.setCustomer(customer); o.setSite(site); o.setAssignee(assignee);
        o.setSlaDueAt(java.time.LocalDateTime.now().plusHours(p == Priority.CRITICAL ? 4 : p == Priority.HIGH ? 8 : p == Priority.MEDIUM ? 24 : 48));
        return repo.save(o);
    }
}
