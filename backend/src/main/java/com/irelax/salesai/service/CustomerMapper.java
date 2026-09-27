package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.CustomerApi;
import com.irelax.salesai.domain.Customer;
import com.irelax.salesai.domain.CustomerNote;
import com.irelax.salesai.domain.CustomerProductInterest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {
    public CustomerApi.Response toResponse(Customer customer, List<CustomerProductInterest> interests, List<CustomerNote> notes) {
        String name = (customer.getFirstName() + " " + (customer.getLastName() == null ? "" : customer.getLastName())).trim();
        List<CustomerApi.InterestResponse> interestResponses = interests.stream()
                .map(i -> new CustomerApi.InterestResponse(
                        i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getInterestLevel(), i.getSource(), i.getConfidence(), i.getReason()))
                .toList();
        List<CustomerApi.NoteResponse> noteResponses = notes.stream()
                .map(n -> new CustomerApi.NoteResponse(n.getId(), n.getBody(), n.getCreatedAt()))
                .toList();
        return new CustomerApi.Response(
                customer.getId(), customer.getFirstName(), customer.getLastName(), name,
                customer.getPhone(), customer.getEmail(), customer.getPreferredChannel(), customer.getSalesStage(),
                customer.getFirstVisitDate(), customer.getFirstVisitLocation(), customer.getFeedback(),
                customer.getBudgetMin(), customer.getBudgetMax(), customer.getNotes(), customer.getLastContactAt(),
                customer.getNextFollowUpAt(), customer.getCreatedAt(), customer.getUpdatedAt(), interestResponses, noteResponses);
    }
}
