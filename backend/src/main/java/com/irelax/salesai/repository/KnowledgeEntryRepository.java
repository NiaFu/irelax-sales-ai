package com.irelax.salesai.repository;

import com.irelax.salesai.domain.KnowledgeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, UUID> {
    List<KnowledgeEntry> findByOwnerIdAndActiveTrueOrderByUpdatedAtDesc(String ownerId);
    List<KnowledgeEntry> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);
}
