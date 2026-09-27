package com.irelax.salesai.api;

import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.domain.KnowledgeEntry;
import com.irelax.salesai.repository.KnowledgeEntryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeEntryRepository repository;
    private final OwnerContext ownerContext;

    public KnowledgeController(KnowledgeEntryRepository repository, OwnerContext ownerContext) {
        this.repository = repository;
        this.ownerContext = ownerContext;
    }

    public record Request(@NotBlank String category, @NotBlank String title, @NotBlank String content, String sourceUrl, Boolean active) {}
    public record Response(UUID id, String category, String title, String content, String sourceUrl, boolean active, Instant updatedAt) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<Response> list() {
        return repository.findByOwnerIdOrderByUpdatedAtDesc(ownerContext.currentOwnerId()).stream().map(this::map).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Response create(@Valid @RequestBody Request request) {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setOwnerId(ownerContext.currentOwnerId());
        entry.setCategory(request.category().trim());
        entry.setTitle(request.title().trim());
        entry.setContent(request.content().trim());
        entry.setSourceUrl(request.sourceUrl());
        entry.setActive(request.active() == null || request.active());
        return map(repository.save(entry));
    }

    private Response map(KnowledgeEntry e) {
        return new Response(e.getId(), e.getCategory(), e.getTitle(), e.getContent(), e.getSourceUrl(), e.isActive(), e.getUpdatedAt());
    }
}
