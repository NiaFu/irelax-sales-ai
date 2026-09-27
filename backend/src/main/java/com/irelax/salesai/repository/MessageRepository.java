package com.irelax.salesai.repository;

import com.irelax.salesai.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findByIdAndOwnerId(UUID id, String ownerId);
    Optional<Message> findByExternalMessageId(String externalMessageId);
    List<Message> findByConversationIdAndOwnerIdOrderByCreatedAtAsc(UUID conversationId, String ownerId);
    List<Message> findByConversationIdAndOwnerIdOrderByCreatedAtDesc(UUID conversationId, String ownerId, Pageable pageable);
    Optional<Message> findFirstByConversationIdAndOwnerIdOrderByCreatedAtDesc(UUID conversationId, String ownerId);
    List<Message> findByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);
}
