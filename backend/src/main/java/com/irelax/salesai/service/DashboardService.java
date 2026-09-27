package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.DashboardApi;
import com.irelax.salesai.api.dto.FollowUpApi;
import com.irelax.salesai.api.dto.MessagingApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.domain.SalesStage;
import com.irelax.salesai.domain.TaskStatus;
import com.irelax.salesai.repository.CustomerRepository;
import com.irelax.salesai.repository.FollowUpTaskRepository;
import com.irelax.salesai.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class DashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Australia/Sydney");
    private final OwnerContext ownerContext;
    private final FollowUpTaskRepository tasks;
    private final CustomerRepository customers;
    private final ConversationRepository conversations;
    private final FollowUpService followUpService;
    private final MessagingService messagingService;

    public DashboardService(OwnerContext ownerContext, FollowUpTaskRepository tasks, CustomerRepository customers,
                            ConversationRepository conversations, FollowUpService followUpService, MessagingService messagingService) {
        this.ownerContext = ownerContext;
        this.tasks = tasks;
        this.customers = customers;
        this.conversations = conversations;
        this.followUpService = followUpService;
        this.messagingService = messagingService;
    }

    @Transactional(readOnly = true)
    public DashboardApi.Response today() {
        String ownerId = ownerContext.currentOwnerId();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant start = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        var open = tasks.findByOwnerIdAndStatusOrderByDueAtAsc(ownerId, TaskStatus.OPEN);
        int overdue = (int) open.stream().filter(t -> t.getDueAt().isBefore(start)).count();
        int dueToday = (int) open.stream().filter(t -> !t.getDueAt().isBefore(start) && t.getDueAt().isBefore(end)).count();
        int newLeads = customers.findByOwnerIdAndSalesStageOrderByUpdatedAtDesc(ownerId, SalesStage.NEW).size();
        int hot = customers.findByOwnerIdAndSalesStageOrderByUpdatedAtDesc(ownerId, SalesStage.HOT).size();
        List<FollowUpApi.Response> priority = open.stream().limit(8).map(followUpService::map).toList();
        List<MessagingApi.ConversationSummary> recent = conversations.findByOwnerIdOrderByLastActivityAtDesc(ownerId).stream()
                .limit(6).map(c -> messagingService.summary(ownerId, c)).toList();
        return new DashboardApi.Response(dueToday, overdue, open.size(), newLeads, hot, priority, recent);
    }
}
