package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.AssistantApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.domain.*;
import com.irelax.salesai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class SalesAssistantQueryService {
    private final OwnerContext ownerContext;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final CustomerProductInterestRepository interests;
    private final FollowUpTaskRepository followUps;

    public SalesAssistantQueryService(OwnerContext ownerContext, CustomerRepository customers, ProductRepository products,
                                      CustomerProductInterestRepository interests, FollowUpTaskRepository followUps) {
        this.ownerContext = ownerContext;
        this.customers = customers;
        this.products = products;
        this.interests = interests;
        this.followUps = followUps;
    }

    @Transactional(readOnly = true)
    public AssistantApi.QueryResponse query(String rawQuery) {
        String ownerId = ownerContext.currentOwnerId();
        String query = rawQuery.toLowerCase(Locale.ROOT).trim();

        if (query.contains("follow") || query.contains("today") || query.contains("overdue")) {
            var tasks = followUps.findByOwnerIdAndStatusOrderByDueAtAsc(ownerId, TaskStatus.OPEN);
            if (tasks.isEmpty()) return new AssistantApi.QueryResponse("You have no open follow-up tasks.");
            String lines = tasks.stream().limit(8).map(t -> "• " + displayName(t.getCustomer()) + " — " + t.getReason() + " — " + t.getDueAt()).reduce((a,b) -> a + "\n" + b).orElse("");
            return new AssistantApi.QueryResponse("Open follow-ups: " + tasks.size() + "\n" + lines);
        }

        if (query.contains("hot")) {
            List<Customer> hot = customers.findByOwnerIdAndSalesStageOrderByUpdatedAtDesc(ownerId, SalesStage.HOT);
            return new AssistantApi.QueryResponse(hot.isEmpty() ? "No customers are currently marked HOT." :
                    "HOT customers: " + hot.stream().map(this::displayName).limit(10).reduce((a,b) -> a + ", " + b).orElse(""));
        }

        if (query.contains("7 day") || query.contains("seven day") || query.contains("no reply") || query.contains("not replied")) {
            Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
            List<Customer> stale = customers.findByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                    .filter(c -> c.getLastContactAt() == null || c.getLastContactAt().isBefore(cutoff))
                    .filter(c -> c.getSalesStage() != SalesStage.SOLD && c.getSalesStage() != SalesStage.LOST && c.getSalesStage() != SalesStage.AFTER_SALES)
                    .toList();
            return new AssistantApi.QueryResponse(stale.isEmpty() ? "No active customers have been untouched for more than 7 days." :
                    "Customers with no contact for 7+ days: " + stale.stream().map(this::displayName).limit(15).reduce((a,b) -> a + ", " + b).orElse(""));
        }

        for (Product product : products.findByOwnerIdAndActiveTrueOrderByNameAsc(ownerId)) {
            if (query.contains(product.getName().toLowerCase(Locale.ROOT))) {
                var matches = interests.findByOwnerIdAndProductIdOrderByUpdatedAtDesc(ownerId, product.getId());
                if (matches.isEmpty()) return new AssistantApi.QueryResponse("No customer product-interest records currently mention " + product.getName() + ".");
                String names = matches.stream().map(i -> displayName(i.getCustomer()) + " (" + i.getSource() + ")").distinct().limit(15).reduce((a,b) -> a + ", " + b).orElse("");
                return new AssistantApi.QueryResponse("Customers linked to " + product.getName() + ": " + names);
            }
        }

        return new AssistantApi.QueryResponse("Try asking: “Who should I follow up today?”, “Show HOT customers”, “Who has not replied for 7 days?”, or mention a product name.");
    }

    private String displayName(Customer c) {
        return (c.getFirstName() + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
    }
}
