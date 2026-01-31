package com.financial.domain.repository;

import com.financial.domain.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = "SELECT * FROM products p WHERE " +
           "(:status IS NULL OR p.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "(:status IS NULL OR p.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Page<Product> findByFilters(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);
}
