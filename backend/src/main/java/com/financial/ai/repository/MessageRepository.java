package com.financial.ai.repository;

import com.financial.ai.domain.SessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<SessionMessage, Long> {

    List<SessionMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);

    long countBySessionId(Long sessionId);
}
