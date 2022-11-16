package lv.rtu.autograderserver.model;

public interface Auditable {
    AuditMetadata getAudit();
    void setAudit(AuditMetadata metadata);
}
