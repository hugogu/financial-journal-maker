package com.financial.domain.repository;

import com.financial.domain.domain.Scenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    Optional<Scenario> findByProductIdAndCode(Long productId, String code);

    boolean existsByProductIdAndCode(Long productId, String code);

    List<Scenario> findByProductId(Long productId);

    int countByProductId(Long productId);

    @Query(value = "SELECT * FROM scenarios s WHERE " +
           "(:productId IS NULL OR s.product_id = :productId) AND " +
           "(:status IS NULL OR s.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(*) FROM scenarios s WHERE " +
           "(:productId IS NULL OR s.product_id = :productId) AND " +
           "(:status IS NULL OR s.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Page<Scenario> findByFilters(
            @Param("productId") Long productId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);
}
