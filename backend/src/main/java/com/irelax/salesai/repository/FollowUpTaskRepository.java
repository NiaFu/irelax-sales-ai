package com.irelax.salesai.repository;

import com.irelax.salesai.domain.FollowUpTask;
import com.irelax.salesai.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, UUID> {
    Optional<FollowUpTask> findByIdAndOwnerId(UUID id, String ownerId);
    List<FollowUpTask> findByOwnerIdAndStatusAndDueAtBeforeOrderByDueAtAsc(String ownerId, TaskStatus status, Instant before);
    List<FollowUpTask> findByOwnerIdAndStatusOrderByDueAtAsc(String ownerId, TaskStatus status);
    List<FollowUpTask> findByOwnerIdAndCustomerIdAndStatusOrderByDueAtAsc(String ownerId, UUID customerId, TaskStatus status);
}
