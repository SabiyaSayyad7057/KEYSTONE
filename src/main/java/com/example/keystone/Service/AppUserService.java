package com.example.keystone.Service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.keystone.Entity.AppUser;
import com.example.keystone.Entity.Customer;
import com.example.keystone.Entity.Role;
import com.example.keystone.Repo.AppUserRepository;
import com.example.keystone.Repo.CustomerRepository;
import com.example.keystone.Repo.SiteRepository;
import com.example.keystone.Repo.WorkOrderRepository;
import com.example.keystone.Entity.Site;

@Service
public class AppUserService {
    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkOrderRepository workOrderRepository;

    public AppUserService(AppUserRepository userRepository, CustomerRepository customerRepository,
                          SiteRepository siteRepository, PasswordEncoder passwordEncoder, WorkOrderRepository workOrderRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
        this.workOrderRepository = workOrderRepository;
    }

    public List<AppUser> getAllUsers() { return userRepository.findAll(); }
    public AppUser getUserById(Long id) { return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id)); }
    public AppUser getUserByEmail(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found: " + email)); }

    public AppUser createUser(String name, String email, String password, Role role) {
        return createUser(name, email, password, role, null);
    }

    @Transactional
    public AppUser createUser(String name, String email, String password, Role role, Customer customer) {
        email = email.trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already exists: " + email);
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCustomer(customer);
        return userRepository.save(user);
    }

    @Transactional
    public AppUser createCustomerAccount(String name, String email, String phone, String address, String password) {
        email = email.trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already exists");
        if (customerRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("Customer email already exists");
        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customerRepository.save(customer);
        Site site = new Site();
        site.setName("Primary Service Location");
        site.setAddress(address == null || address.isBlank() ? "Address to be confirmed" : address.trim());
        site.setCustomer(customer);
        siteRepository.save(site);
        return createUser(name.trim(), email, password, Role.CUSTOMER, customer);
    }

    public AppUser saveUser(AppUser user) { return userRepository.save(user); }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = getUserById(id);
        if (workOrderRepository.findByAssigneeIdOrderByCreatedAtDesc(id).stream().findAny().isPresent()) {
            throw new IllegalStateException("User cannot be deleted while work orders are assigned to this user.");
        }
        userRepository.delete(user);
    }
}
