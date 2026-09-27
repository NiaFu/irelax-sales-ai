package com.irelax.salesai.repository;

import com.irelax.salesai.domain.CustomerProductInterest;
import com.irelax.salesai.domain.InterestSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerProductInterestRepository extends JpaRepository<CustomerProductInterest, UUID> {
    List<CustomerProductInterest> findByOwnerIdAndCustomerIdOrderByUpdatedAtDesc(String ownerId, UUID customerId);
    Optional<CustomerProductInterest> findByOwnerIdAndCustomerIdAndProductIdAndSource(String ownerId, UUID customerId, UUID productId, InterestSource source);
    List<CustomerProductInterest> findByOwnerIdAndProductIdOrderByUpdatedAtDesc(String ownerId, UUID productId);
}
