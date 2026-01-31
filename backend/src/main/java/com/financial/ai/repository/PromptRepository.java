package com.financial.ai.repository;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptRepository extends JpaRepository<PromptTemplate, Long> {

    List<PromptTemplate> findByDesignPhase(DesignPhase designPhase);

    Optional<PromptTemplate> findByDesignPhaseAndIsActiveTrue(DesignPhase designPhase);

    List<PromptTemplate> findByNameOrderByVersionDesc(String name);

    Optional<PromptTemplate> findByNameAndVersion(String name, Integer version);
}
