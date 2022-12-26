package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "publications")
public class Publication implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Embedded
    private AuditMetadata auditMetadata;

    @Override
    public AuditMetadata getAudit() {
        return auditMetadata;
    }

    @Override
    public void setAudit(AuditMetadata metadata) {
        this.auditMetadata = metadata;
    }
}
