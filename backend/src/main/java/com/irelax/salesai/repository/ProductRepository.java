package com.irelax.salesai.repository;

import com.irelax.salesai.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndOwnerId(UUID id, String ownerId);
    Optional<Product> findByOwnerIdAndNameIgnoreCase(String ownerId, String name);
    List<Product> findByOwnerIdAndActiveTrueOrderByNameAsc(String ownerId);
    List<Product> findByOwnerIdOrderByNameAsc(String ownerId);
}
