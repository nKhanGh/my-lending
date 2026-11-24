package com.nkhang.lending.audit;

import com.nkhang.lending.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final CurrentUser currentUser;

    public void record(UUID subject, String action, String resourceType, UUID resourceId, String detail) {
        jdbc.update("INSERT INTO audit_events(id,actor_user_id,subject_user_id,action,resource_type,resource_id,detail,occurred_at) VALUES (?,?,?,?,?,?,?,?)",
                UUID.randomUUID(), currentUser.optionalId(), subject, action, resourceType, resourceId,
                detail, Timestamp.from(clock.instant()));
    }
    public List<AuditEventResponse> own() { return forUser(currentUser.id()); }
    public List<AuditEventResponse> forUser(UUID userId) {
        return jdbc.query("SELECT * FROM audit_events WHERE subject_user_id=? ORDER BY occurred_at DESC, id DESC",
                (rs, row) -> new AuditEventResponse(rs.getObject("id", UUID.class),
                        rs.getObject("actor_user_id", UUID.class), rs.getObject("subject_user_id", UUID.class),
                        rs.getString("action"), rs.getString("resource_type"), rs.getObject("resource_id", UUID.class),
                        rs.getString("detail"), rs.getTimestamp("occurred_at").toInstant()), userId);
    }
}
