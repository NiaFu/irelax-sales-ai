package com.irelax.salesai.repository;

import com.irelax.salesai.domain.ContactChannel;
import com.irelax.salesai.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndOwnerId(UUID id, String ownerId);
    Optional<Conversation> findByOwnerIdAndCustomerIdAndChannel(String ownerId, UUID customerId, ContactChannel channel);
    List<Conversation> findByOwnerIdOrderByLastActivityAtDesc(String ownerId);
}
