package com.irelax.salesai.api;

import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.domain.SalesAssistantProfile;
import com.irelax.salesai.repository.SalesAssistantProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final SalesAssistantProfileRepository repository;
    private final OwnerContext ownerContext;

    public ProfileController(SalesAssistantProfileRepository repository, OwnerContext ownerContext) {
        this.repository = repository;
        this.ownerContext = ownerContext;
    }

    public record Request(@NotBlank String displayName, @NotBlank String tone, @NotBlank String rules) {}
    public record Response(String displayName, String tone, String rules) {}

    @GetMapping
    @Transactional
    public Response get() {
        String ownerId = ownerContext.currentOwnerId();
        SalesAssistantProfile profile = repository.findByOwnerId(ownerId).orElseGet(() -> {
            SalesAssistantProfile created = new SalesAssistantProfile();
            created.setOwnerId(ownerId);
            created.setDisplayName("Nia");
            created.setTone("polite, natural, concise, low-pressure, not corporate");
            created.setRules("Do not invent prices, discounts, warranties, stock or delivery promises. Keep SMS replies short and human.");
            return repository.save(created);
        });
        return new Response(profile.getDisplayName(), profile.getTone(), profile.getRules());
    }

    @PutMapping
    @Transactional
    public Response update(@Valid @RequestBody Request request) {
        String ownerId = ownerContext.currentOwnerId();
        SalesAssistantProfile profile = repository.findByOwnerId(ownerId).orElseGet(SalesAssistantProfile::new);
        profile.setOwnerId(ownerId);
        profile.setDisplayName(request.displayName().trim());
        profile.setTone(request.tone().trim());
        profile.setRules(request.rules().trim());
        profile = repository.save(profile);
        return new Response(profile.getDisplayName(), profile.getTone(), profile.getRules());
    }
}
