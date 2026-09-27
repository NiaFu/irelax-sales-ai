package com.irelax.salesai.api.dto;

import java.util.List;

public final class DashboardApi {
    private DashboardApi() {}

    public record Response(
            int dueToday,
            int overdue,
            int openTasks,
            int newLeads,
            int hotCustomers,
            List<FollowUpApi.Response> priorityTasks,
            List<MessagingApi.ConversationSummary> recentConversations
    ) {}
}
