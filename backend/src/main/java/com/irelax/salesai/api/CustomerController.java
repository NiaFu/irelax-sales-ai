package com.irelax.salesai.api;

import com.irelax.salesai.api.dto.CustomerApi;
import com.irelax.salesai.domain.SalesStage;
import com.irelax.salesai.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public List<CustomerApi.Response> list(@RequestParam(required = false) String search,
                                           @RequestParam(required = false) SalesStage stage) {
        return service.list(search, stage);
    }

    @GetMapping("/{id}")
    public CustomerApi.Response get(@PathVariable UUID id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerApi.Response create(@Valid @RequestBody CustomerApi.UpsertRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public CustomerApi.Response update(@PathVariable UUID id, @Valid @RequestBody CustomerApi.UpsertRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }

    @PostMapping("/{id}/interests")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerApi.InterestResponse addInterest(@PathVariable UUID id, @RequestBody CustomerApi.InterestRequest request) {
        return service.addCustomerInterest(id, request);
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerApi.NoteResponse addNote(@PathVariable UUID id, @Valid @RequestBody CustomerApi.NoteRequest request) {
        return service.addNote(id, request);
    }
}
