package com.example.keystone.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.keystone.Entity.AppUser;
import com.example.keystone.Entity.Customer;
import com.example.keystone.Entity.Priority;
import com.example.keystone.Entity.Role;
import com.example.keystone.Entity.Site;
import com.example.keystone.Entity.WorkOrder;
import com.example.keystone.Entity.WorkOrderStatus;
import com.example.keystone.Entity.WorkOrderStatusHistory;
import com.example.keystone.Repo.AppUserRepository;
import com.example.keystone.Repo.CustomerRepository;
import com.example.keystone.Repo.SiteRepository;
import com.example.keystone.Repo.StatusHistoryRepository;
import com.example.keystone.Repo.WorkOrderRepository;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final AppUserRepository userRepository;
    private final StatusHistoryRepository historyRepository;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            CustomerRepository customerRepository,
            SiteRepository siteRepository,
            AppUserRepository userRepository,
            StatusHistoryRepository historyRepository) {

        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    // =====================================================
    // GET ALL WORK ORDERS
    // =====================================================

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    public List<WorkOrder> getOrdersForUser(AppUser user) {
        if (user.getRole() == Role.CUSTOMER) {
            if (user.getCustomer() == null) return List.of();
            return workOrderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getCustomer().getId());
        }
        if (user.getRole() == Role.TECHNICIAN) {
            return workOrderRepository.findByAssigneeIdOrderByCreatedAtDesc(user.getId());
        }
        return workOrderRepository.findAll();
    }

    public List<Site> getSitesForCustomer(Long customerId) {
        return siteRepository.findAll().stream()
                .filter(s -> s.getCustomer() != null && customerId.equals(s.getCustomer().getId()))
                .toList();
    }

    @Transactional
    public WorkOrder createCustomerRequest(String title, String description, Priority priority, Long siteId, AppUser customerUser) {
        if (customerUser.getRole() != Role.CUSTOMER || customerUser.getCustomer() == null) {
            throw new AccessDeniedException("Only customer accounts can create service requests");
        }
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        if (!customerUser.getCustomer().getId().equals(site.getCustomer().getId())) {
            throw new AccessDeniedException("You can only request service for your own site");
        }
        WorkOrder order = new WorkOrder();
        order.setCode("WO-" + System.currentTimeMillis());
        order.setTitle(title);
        order.setDescription(description);
        order.setPriority(priority);
        order.setStatus(WorkOrderStatus.NEW);
        order.setCustomer(customerUser.getCustomer());
        order.setSite(site);
        order.setSlaDueAt(calculateSla(priority));
        return workOrderRepository.save(order);
    }

    // =====================================================
    // GET WORK ORDER BY ID
    // =====================================================

    public WorkOrder getWorkOrderById(Long id) {

        return workOrderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Work order not found with ID: " + id
                        )
                );
    }

    // =====================================================
    // GET ALL CUSTOMERS
    // =====================================================

    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // =====================================================
    // GET ALL SITES
    // =====================================================

    public List<Site> getAllSites() {

        return siteRepository.findAll();
    }

    // =====================================================
    // GET TECHNICIANS
    // =====================================================

    public List<AppUser> getTechnicians() {

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == Role.TECHNICIAN)
                .toList();
    }

    // =====================================================
    // CREATE WORK ORDER
    // =====================================================

    @Transactional
    public WorkOrder createWorkOrder(
            String title,
            String description,
            Priority priority,
            Long customerId,
            Long siteId) {

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found with ID: "
                                                + customerId
                                )
                        );

        Site site =
                siteRepository.findById(siteId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Site not found with ID: "
                                                + siteId
                                )
                        );

        WorkOrder order = new WorkOrder();

        order.setCode(
                "WO-" + System.currentTimeMillis()
        );

        order.setTitle(title);
        order.setDescription(description);
        order.setPriority(priority);

        order.setStatus(
                WorkOrderStatus.NEW
        );

        order.setCustomer(customer);
        order.setSite(site);

        order.setSlaDueAt(
                calculateSla(priority)
        );

        return workOrderRepository.save(order);
    }

    // =====================================================
    // CALCULATE SLA
    // =====================================================

    private LocalDateTime calculateSla(
            Priority priority) {

        return switch (priority) {

            case CRITICAL ->
                    LocalDateTime.now()
                            .plusHours(4);

            case HIGH ->
                    LocalDateTime.now()
                            .plusHours(8);

            case MEDIUM ->
                    LocalDateTime.now()
                            .plusHours(24);

            case LOW ->
                    LocalDateTime.now()
                            .plusHours(48);
        };
    }

    // =====================================================
    // ASSIGN TECHNICIAN
    // =====================================================

    @Transactional
    public WorkOrder assignTechnician(
            Long workOrderId,
            Long technicianId) {

        WorkOrder order =
                getWorkOrderById(workOrderId);

        AppUser technician =
                userRepository.findById(technicianId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Technician not found with ID: "
                                                + technicianId
                                )
                        );

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new IllegalArgumentException(
                    "Selected user is not a technician"
            );
        }

        order.setAssignee(technician);

        order.setStatus(
                WorkOrderStatus.ASSIGNED
        );

        return workOrderRepository.save(order);
    }

    // =====================================================
    // CHANGE STATUS
    // =====================================================

    @Transactional
    public WorkOrder changeStatus(
            Long workOrderId,
            WorkOrderStatus newStatus,
            String note,
            String userEmail) {

        WorkOrder order =
                getWorkOrderById(workOrderId);

        AppUser user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found with email: "
                                                + userEmail
                                )
                        );

        WorkOrderStatus oldStatus =
                order.getStatus();

        // Check status transition
        if (!isAllowedTransition(
                oldStatus,
                newStatus)) {

            throw new IllegalStateException(
                    "Invalid status transition: "
                            + oldStatus
                            + " -> "
                            + newStatus
            );
        }

        // Technician can only update their assigned work order
        if (user.getRole() == Role.TECHNICIAN) {

            if (order.getAssignee() == null ||
                    !order.getAssignee()
                            .getId()
                            .equals(user.getId())) {

                throw new AccessDeniedException(
                        "This work order is not assigned to you"
                );
            }
        }

        order.setStatus(newStatus);

        WorkOrder saved =
                workOrderRepository.save(order);

        // Create history record
        WorkOrderStatusHistory history =
                new WorkOrderStatusHistory();

        history.setWorkOrder(saved);

        history.setFromStatus(
                oldStatus.name()
        );

        history.setToStatus(
                newStatus.name()
        );

        history.setChangedBy(user);

        history.setNote(note);

        historyRepository.save(history);

        return saved;
    }

    // =====================================================
    // STATUS TRANSITIONS
    // =====================================================

    private boolean isAllowedTransition(
            WorkOrderStatus from,
            WorkOrderStatus to) {

        return switch (from) {

            case NEW ->
                    to == WorkOrderStatus.ASSIGNED
                    || to == WorkOrderStatus.CANCELLED;

            case ASSIGNED ->
                    to == WorkOrderStatus.IN_PROGRESS
                    || to == WorkOrderStatus.CANCELLED;

            case IN_PROGRESS ->
                    to == WorkOrderStatus.ON_HOLD
                    || to == WorkOrderStatus.COMPLETED;

            case ON_HOLD ->
                    to == WorkOrderStatus.IN_PROGRESS
                    || to == WorkOrderStatus.CANCELLED;

            case COMPLETED ->
                    to == WorkOrderStatus.CLOSED;

            case CLOSED, CANCELLED ->
                    false;
        };
    }

    // =====================================================
    // ALLOWED NEXT STATUSES
    // =====================================================

    public List<WorkOrderStatus> getAllowedNextStatuses(WorkOrder order, AppUser user) {
        WorkOrderStatus from = order.getStatus();
        if (from == null) return List.of();

        if (user.getRole() == Role.TECHNICIAN) {
            if (order.getAssignee() == null || !order.getAssignee().getId().equals(user.getId())) {
                return List.of();
            }
        }

        return switch (from) {
            case NEW -> List.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED);
            case ASSIGNED -> List.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED);
            case IN_PROGRESS -> List.of(WorkOrderStatus.ON_HOLD, WorkOrderStatus.COMPLETED);
            case ON_HOLD -> List.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED);
            case COMPLETED -> List.of(WorkOrderStatus.CLOSED);
            case CLOSED, CANCELLED -> List.of();
        };
    }

    // =====================================================
    // GET STATUS HISTORY
    // =====================================================

    public List<WorkOrderStatusHistory> getHistory(
            Long workOrderId) {

        return historyRepository
                .findByWorkOrderIdOrderByChangedAtDesc(
                        workOrderId
                );
    }
}