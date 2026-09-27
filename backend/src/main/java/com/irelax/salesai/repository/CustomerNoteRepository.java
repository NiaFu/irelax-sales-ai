package com.irelax.salesai.repository;

import com.irelax.salesai.domain.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerNoteRepository extends JpaRepository<CustomerNote, UUID> {
    List<CustomerNote> findByOwnerIdAndCustomerIdOrderByCreatedAtDesc(String ownerId, UUID customerId);
}
