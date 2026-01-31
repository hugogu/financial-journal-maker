package com.financial.ai.repository;

import com.financial.ai.domain.AnalysisSession;
import com.financial.ai.domain.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<AnalysisSession, Long> {

    Page<AnalysisSession> findByStatus(SessionStatus status, Pageable pageable);

    Page<AnalysisSession> findByAnalystId(String analystId, Pageable pageable);

    Page<AnalysisSession> findByAnalystIdAndStatus(String analystId, SessionStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM AnalysisSession s WHERE s.analystId = :analystId AND s.status IN :statuses")
    long countByAnalystIdAndStatusIn(@Param("analystId") String analystId, @Param("statuses") List<SessionStatus> statuses);

    List<AnalysisSession> findByAnalystIdAndStatusIn(String analystId, List<SessionStatus> statuses);
}
