package com.financial.ai.repository;

import com.financial.ai.domain.ExportArtifact;
import com.financial.ai.domain.ExportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportArtifactRepository extends JpaRepository<ExportArtifact, Long> {

    List<ExportArtifact> findBySessionId(Long sessionId);

    List<ExportArtifact> findBySessionIdAndArtifactType(Long sessionId, ExportType artifactType);
}
