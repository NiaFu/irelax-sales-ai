package com.irelax.salesai.api;

import com.irelax.salesai.api.dto.FollowUpApi;
import com.irelax.salesai.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follow-ups")
public class FollowUpController {
    private final FollowUpService service;

    public FollowUpController(FollowUpService service) { this.service = service; }

    @GetMapping
    public List<FollowUpApi.Response> list() { return service.listOpen(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpApi.Response create(@Valid @RequestBody FollowUpApi.CreateRequest request) { return service.create(request); }

    @PostMapping("/{id}/complete")
    public FollowUpApi.Response complete(@PathVariable UUID id) { return service.complete(id); }
}
