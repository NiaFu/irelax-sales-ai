package com.irelax.salesai.repository;

import com.irelax.salesai.domain.AiSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, UUID> {
    Optional<AiSuggestion> findByIdAndOwnerId(UUID id, String ownerId);
    Optional<AiSuggestion> findFirstByOwnerIdAndMessageIdOrderByCreatedAtDesc(String ownerId, UUID messageId);
    Optional<AiSuggestion> findFirstByOwnerIdAndMessageConversationIdOrderByCreatedAtDesc(String ownerId, UUID conversationId);
}
