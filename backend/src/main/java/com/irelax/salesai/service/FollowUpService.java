package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.FollowUpApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.common.ResourceNotFoundException;
import com.irelax.salesai.domain.*;
import com.irelax.salesai.repository.CustomerRepository;
import com.irelax.salesai.repository.FollowUpTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class FollowUpService {
    private final FollowUpTaskRepository tasks;
    private final CustomerRepository customers;
    private final OwnerContext ownerContext;

    public FollowUpService(FollowUpTaskRepository tasks, CustomerRepository customers, OwnerContext ownerContext) {
        this.tasks = tasks;
        this.customers = customers;
        this.ownerContext = ownerContext;
    }

    @Transactional(readOnly = true)
    public List<FollowUpApi.Response> listOpen() {
        String ownerId = ownerContext.currentOwnerId();
        return tasks.findByOwnerIdAndStatusOrderByDueAtAsc(ownerId, TaskStatus.OPEN).stream().map(this::map).toList();
    }

    @Transactional
    public FollowUpApi.Response create(FollowUpApi.CreateRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Customer customer = customers.findByIdAndOwnerId(request.customerId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        FollowUpTask task = createTask(ownerId, customer,
                request.type() == null ? FollowUpType.GENERAL_FOLLOW_UP : request.type(),
                request.dueAt(), request.reason(), request.priority() == null ? TaskPriority.NORMAL : request.priority(), false);
        customer.setNextFollowUpAt(request.dueAt());
        return map(task);
    }

    @Transactional
    public FollowUpApi.Response complete(UUID id) {
        String ownerId = ownerContext.currentOwnerId();
        FollowUpTask task = tasks.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up task not found"));
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        task = tasks.save(task);
        refreshCustomerNextFollowUp(ownerId, task.getCustomer());
        return map(task);
    }

    @Transactional
    public FollowUpTask createAiSuggested(String ownerId, Customer customer, Instant dueAt, String reason) {
        Instant safeDueAt = dueAt == null ? Instant.now().plus(3, ChronoUnit.DAYS) : dueAt;
        FollowUpTask existing = tasks.findByOwnerIdAndCustomerIdAndStatusOrderByDueAtAsc(ownerId, customer.getId(), TaskStatus.OPEN)
                .stream()
                .filter(FollowUpTask::isAiGenerated)
                .filter(t -> Math.abs(ChronoUnit.HOURS.between(t.getDueAt(), safeDueAt)) <= 12)
                .findFirst().orElse(null);
        if (existing != null) return existing;
        FollowUpTask task = createTask(ownerId, customer, FollowUpType.GENERAL_FOLLOW_UP, safeDueAt,
                reason == null || reason.isBlank() ? "AI suggested follow-up" : reason,
                TaskPriority.NORMAL, true);
        if (customer.getNextFollowUpAt() == null || safeDueAt.isBefore(customer.getNextFollowUpAt())) {
            customer.setNextFollowUpAt(safeDueAt);
        }
        return task;
    }

    @Transactional
    public void createPostSaleTasks(String ownerId, Customer customer) {
        List<FollowUpTask> current = tasks.findByOwnerIdAndCustomerIdAndStatusOrderByDueAtAsc(ownerId, customer.getId(), TaskStatus.OPEN);
        Instant now = Instant.now();
        if (current.stream().noneMatch(t -> t.getType() == FollowUpType.DELIVERY_CHECK)) {
            createTask(ownerId, customer, FollowUpType.DELIVERY_CHECK, now.plus(2, ChronoUnit.DAYS),
                    "Confirm delivery or installation progress", TaskPriority.NORMAL, false);
        }
        if (current.stream().noneMatch(t -> t.getType() == FollowUpType.POST_SALE_7_DAY)) {
            createTask(ownerId, customer, FollowUpType.POST_SALE_7_DAY, now.plus(7, ChronoUnit.DAYS),
                    "Check the customer's first-week experience", TaskPriority.NORMAL, false);
        }
        if (current.stream().noneMatch(t -> t.getType() == FollowUpType.POST_SALE_30_DAY)) {
            createTask(ownerId, customer, FollowUpType.POST_SALE_30_DAY, now.plus(30, ChronoUnit.DAYS),
                    "30-day customer care check-in", TaskPriority.LOW, false);
        }
        refreshCustomerNextFollowUp(ownerId, customer);
    }

    @Transactional(readOnly = true)
    public List<FollowUpApi.Response> dueBefore(String ownerId, Instant before) {
        return tasks.findByOwnerIdAndStatusAndDueAtBeforeOrderByDueAtAsc(ownerId, TaskStatus.OPEN, before)
                .stream().map(this::map).toList();
    }

    private FollowUpTask createTask(String ownerId, Customer customer, FollowUpType type, Instant dueAt,
                                    String reason, TaskPriority priority, boolean aiGenerated) {
        FollowUpTask task = new FollowUpTask();
        task.setOwnerId(ownerId);
        task.setCustomer(customer);
        task.setType(type);
        task.setDueAt(dueAt);
        task.setStatus(TaskStatus.OPEN);
        task.setReason(reason);
        task.setPriority(priority);
        task.setAiGenerated(aiGenerated);
        return tasks.save(task);
    }

    private void refreshCustomerNextFollowUp(String ownerId, Customer customer) {
        tasks.findByOwnerIdAndCustomerIdAndStatusOrderByDueAtAsc(ownerId, customer.getId(), TaskStatus.OPEN)
                .stream().findFirst().ifPresentOrElse(
                        task -> customer.setNextFollowUpAt(task.getDueAt()),
                        () -> customer.setNextFollowUpAt(null));
        customers.save(customer);
    }

    public FollowUpApi.Response map(FollowUpTask task) {
        Customer c = task.getCustomer();
        String name = (c.getFirstName() + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
        return new FollowUpApi.Response(task.getId(), c.getId(), name, task.getType(), task.getDueAt(), task.getStatus(),
                task.getReason(), task.getPriority(), task.isAiGenerated(), task.getCompletedAt());
    }
}
