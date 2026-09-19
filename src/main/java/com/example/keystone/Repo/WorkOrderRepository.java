package com.example.keystone.Repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.keystone.Entity.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<WorkOrder> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId);
    long countByCustomerId(Long customerId);
    long countByAssigneeId(Long assigneeId);
}
