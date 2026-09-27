package com.irelax.salesai.service;

import com.irelax.salesai.api.dto.CustomerApi;
import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.common.PhoneNumberNormalizer;
import com.irelax.salesai.common.ResourceNotFoundException;
import com.irelax.salesai.domain.*;
import com.irelax.salesai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final CustomerProductInterestRepository interests;
    private final ProductRepository products;
    private final CustomerNoteRepository notes;
    private final PhoneNumberNormalizer phoneNormalizer;
    private final OwnerContext ownerContext;
    private final CustomerMapper mapper;
    private final FollowUpService followUpService;

    public CustomerService(CustomerRepository customers,
                           CustomerProductInterestRepository interests,
                           ProductRepository products,
                           CustomerNoteRepository notes,
                           PhoneNumberNormalizer phoneNormalizer,
                           OwnerContext ownerContext,
                           CustomerMapper mapper,
                           FollowUpService followUpService) {
        this.customers = customers;
        this.interests = interests;
        this.products = products;
        this.notes = notes;
        this.phoneNormalizer = phoneNormalizer;
        this.ownerContext = ownerContext;
        this.mapper = mapper;
        this.followUpService = followUpService;
    }

    @Transactional(readOnly = true)
    public List<CustomerApi.Response> list(String search, SalesStage stage) {
        String ownerId = ownerContext.currentOwnerId();
        List<Customer> result;
        if (stage != null) {
            result = customers.findByOwnerIdAndSalesStageOrderByUpdatedAtDesc(ownerId, stage);
        } else if (search != null && !search.isBlank()) {
            result = customers.findByOwnerIdAndFirstNameContainingIgnoreCaseOrOwnerIdAndLastNameContainingIgnoreCaseOrderByUpdatedAtDesc(
                    ownerId, search.trim(), ownerId, search.trim());
        } else {
            result = customers.findByOwnerIdOrderByUpdatedAtDesc(ownerId);
        }
        return result.stream().map(c -> map(ownerId, c)).toList();
    }

    @Transactional(readOnly = true)
    public CustomerApi.Response get(UUID id) {
        String ownerId = ownerContext.currentOwnerId();
        return map(ownerId, require(ownerId, id));
    }

    @Transactional
    public CustomerApi.Response create(CustomerApi.UpsertRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        String phone = phoneNormalizer.normalize(request.phone());
        if (customers.findByOwnerIdAndPhone(ownerId, phone).isPresent()) {
            throw new IllegalStateException("A customer already exists with phone " + phone);
        }
        Customer customer = new Customer();
        customer.setOwnerId(ownerId);
        apply(customer, request, phone);
        customer = customers.save(customer);
        return map(ownerId, customer);
    }

    @Transactional
    public CustomerApi.Response update(UUID id, CustomerApi.UpsertRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Customer customer = require(ownerId, id);
        SalesStage previousStage = customer.getSalesStage();
        String phone = phoneNormalizer.normalize(request.phone());
        customers.findByOwnerIdAndPhone(ownerId, phone)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalStateException("Another customer already uses phone " + phone); });
        apply(customer, request, phone);
        customer = customers.save(customer);
        if (previousStage != SalesStage.SOLD && customer.getSalesStage() == SalesStage.SOLD) {
            followUpService.createPostSaleTasks(ownerId, customer);
        }
        return map(ownerId, customer);
    }

    @Transactional
    public void delete(UUID id) {
        String ownerId = ownerContext.currentOwnerId();
        customers.delete(require(ownerId, id));
    }

    @Transactional
    public CustomerApi.InterestResponse addCustomerInterest(UUID customerId, CustomerApi.InterestRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Customer customer = require(ownerId, customerId);
        Product product = products.findByIdAndOwnerId(request.productId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        CustomerProductInterest interest = interests.findByOwnerIdAndCustomerIdAndProductIdAndSource(ownerId, customerId, product.getId(), InterestSource.CUSTOMER)
                .orElseGet(CustomerProductInterest::new);
        interest.setOwnerId(ownerId);
        interest.setCustomer(customer);
        interest.setProduct(product);
        interest.setSource(InterestSource.CUSTOMER);
        interest.setInterestLevel(request.level() == null ? InterestLevel.MEDIUM : request.level());
        interest.setConfidence(BigDecimal.ONE);
        interest.setReason(request.reason());
        interest = interests.save(interest);
        return new CustomerApi.InterestResponse(interest.getId(), product.getId(), product.getName(), interest.getInterestLevel(), interest.getSource(), interest.getConfidence(), interest.getReason());
    }

    @Transactional
    public CustomerApi.NoteResponse addNote(UUID customerId, CustomerApi.NoteRequest request) {
        String ownerId = ownerContext.currentOwnerId();
        Customer customer = require(ownerId, customerId);
        CustomerNote note = new CustomerNote();
        note.setOwnerId(ownerId);
        note.setCustomer(customer);
        note.setBody(request.body().trim());
        note = notes.save(note);
        return new CustomerApi.NoteResponse(note.getId(), note.getBody(), note.getCreatedAt());
    }

    @Transactional
    public Customer findOrCreateFromPhone(String ownerId, String rawPhone) {
        String phone = phoneNormalizer.normalize(rawPhone);
        return customers.findByOwnerIdAndPhone(ownerId, phone).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setOwnerId(ownerId);
            customer.setFirstName("New lead");
            customer.setPhone(phone);
            customer.setPreferredChannel(ContactChannel.SMS);
            customer.setSalesStage(SalesStage.NEW);
            return customers.save(customer);
        });
    }

    @Transactional(readOnly = true)
    public Customer require(String ownerId, UUID id) {
        return customers.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private void apply(Customer c, CustomerApi.UpsertRequest r, String normalizedPhone) {
        c.setFirstName(r.firstName().trim());
        c.setLastName(blankToNull(r.lastName()));
        c.setPhone(normalizedPhone);
        c.setEmail(blankToNull(r.email()));
        c.setPreferredChannel(r.preferredChannel() == null ? ContactChannel.SMS : r.preferredChannel());
        c.setSalesStage(r.salesStage() == null ? (c.getSalesStage() == null ? SalesStage.NEW : c.getSalesStage()) : r.salesStage());
        c.setFirstVisitDate(r.firstVisitDate());
        c.setFirstVisitLocation(blankToNull(r.firstVisitLocation()));
        c.setFeedback(blankToNull(r.feedback()));
        c.setBudgetMin(r.budgetMin());
        c.setBudgetMax(r.budgetMax());
        c.setNotes(blankToNull(r.notes()));
        c.setNextFollowUpAt(r.nextFollowUpAt());
    }

    private CustomerApi.Response map(String ownerId, Customer customer) {
        return mapper.toResponse(customer,
                interests.findByOwnerIdAndCustomerIdOrderByUpdatedAtDesc(ownerId, customer.getId()),
                notes.findByOwnerIdAndCustomerIdOrderByCreatedAtDesc(ownerId, customer.getId()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
