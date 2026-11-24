package com.nkhang.lending.audit;
import java.time.Instant;
import java.util.UUID;
public record AuditEventResponse(UUID id, UUID actorUserId, UUID subjectUserId, String action,
        String resourceType, UUID resourceId, String detail, Instant occurredAt) {}
