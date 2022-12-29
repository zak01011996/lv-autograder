package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "participants")
public class Participant implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "identifier", unique = true)
    private String identifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    private Publication publication;

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
