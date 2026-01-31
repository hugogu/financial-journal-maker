package com.financial.ai.repository;

import com.financial.ai.domain.AIConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIConfigRepository extends JpaRepository<AIConfiguration, Long> {

    Optional<AIConfiguration> findByProviderName(String providerName);

    Optional<AIConfiguration> findByIsActiveTrue();

    List<AIConfiguration> findAllByOrderByPriorityAsc();

    boolean existsByProviderName(String providerName);
}
