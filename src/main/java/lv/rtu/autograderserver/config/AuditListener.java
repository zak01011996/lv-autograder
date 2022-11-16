package lv.rtu.autograderserver.config;

import lv.rtu.autograderserver.model.AuditMetadata;
import lv.rtu.autograderserver.model.Auditable;
import lv.rtu.autograderserver.security.LoggedInUser;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

public class AuditListener {

    @PrePersist
    public void setCreatedOn(Object entity) {
        if (entity instanceof Auditable) {
            Auditable auditable = (Auditable) entity;
            AuditMetadata audit = auditable.getAudit();
            if (audit == null) {
                audit = new AuditMetadata();
                auditable.setAudit(audit);
            }

            LocalDateTime now = LocalDateTime.now();
            audit.setCreatedAt(now);
            audit.setCreatedBy(fetchUsername());
            audit.setUpdatedAt(now);
            audit.setUpdatedBy(fetchUsername());
        }
    }

    @PreUpdate
    public void setUpdatedOn(Object entity) {
        if (entity instanceof Auditable) {
            Auditable auditable = (Auditable) entity;
            AuditMetadata audit = auditable.getAudit();
            if (audit == null) {
                audit = new AuditMetadata();
                auditable.setAudit(audit);
            }

            audit.setUpdatedAt(LocalDateTime.now());
            audit.setUpdatedBy(fetchUsername());
        }
    }

    private String fetchUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof LoggedInUser) {
            return ((LoggedInUser) principal).getUsername();
        }

        return "UNKNOWN";
    }
}
