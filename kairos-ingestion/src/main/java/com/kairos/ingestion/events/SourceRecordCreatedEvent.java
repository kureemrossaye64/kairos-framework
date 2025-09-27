package com.kairos.ingestion.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

@Getter
public class SourceRecordCreatedEvent extends ApplicationEvent {
    private final UUID sourceRecordId;

    public SourceRecordCreatedEvent(Object source, UUID sourceRecordId) {
        super(source);
        this.sourceRecordId = sourceRecordId;
    }
}