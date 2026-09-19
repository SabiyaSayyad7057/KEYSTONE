package com.example.keystone.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.keystone.Entity.WorkOrderStatusHistory;

@Repository
public interface StatusHistoryRepository
        extends JpaRepository<WorkOrderStatusHistory, Long> {

    List<WorkOrderStatusHistory> findByWorkOrderIdOrderByChangedAtDesc(
            Long workOrderId
    );
}