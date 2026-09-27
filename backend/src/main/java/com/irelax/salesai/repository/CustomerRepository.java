package com.irelax.salesai.repository;

import com.irelax.salesai.domain.Customer;
import com.irelax.salesai.domain.SalesStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndOwnerId(UUID id, String ownerId);
    Optional<Customer> findByOwnerIdAndPhone(String ownerId, String phone);
    List<Customer> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);
    List<Customer> findByOwnerIdAndSalesStageOrderByUpdatedAtDesc(String ownerId, SalesStage salesStage);
    List<Customer> findByOwnerIdAndFirstNameContainingIgnoreCaseOrOwnerIdAndLastNameContainingIgnoreCaseOrderByUpdatedAtDesc(
            String ownerId1, String firstName, String ownerId2, String lastName);
}
