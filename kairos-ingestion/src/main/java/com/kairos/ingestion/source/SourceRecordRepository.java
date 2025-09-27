package com.kairos.ingestion.source;

import com.kairos.core.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SourceRecordRepository extends JpaRepository<SourceRecord, UUID> {
}