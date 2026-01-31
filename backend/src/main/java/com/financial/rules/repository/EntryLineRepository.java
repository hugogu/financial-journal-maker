package com.financial.rules.repository;

import com.financial.rules.domain.EntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntryLineRepository extends JpaRepository<EntryLine, Long> {

    List<EntryLine> findByTemplateIdOrderBySequenceNumber(Long templateId);

    void deleteByTemplateId(Long templateId);

    boolean existsByAccountCode(String accountCode);

    List<EntryLine> findByAccountCode(String accountCode);
}
