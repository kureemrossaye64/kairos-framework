package com.kairos.ingestion.source;

import com.kairos.core.entity.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "source_records")
@Getter
@Setter
@NoArgsConstructor
public class SourceRecord extends BaseEntity {

    @Column(nullable = false, unique = true, length = 1024)
    private String gcsUri;

    @Column(nullable = false)
    private String sourceName;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.PENDING;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadataManifest;

    @Column(columnDefinition = "TEXT")
    private String failureReason;
}