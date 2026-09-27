package com.irelax.salesai.repository;

import com.irelax.salesai.domain.SalesAssistantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesAssistantProfileRepository extends JpaRepository<SalesAssistantProfile, UUID> {
    Optional<SalesAssistantProfile> findByOwnerId(String ownerId);
}
