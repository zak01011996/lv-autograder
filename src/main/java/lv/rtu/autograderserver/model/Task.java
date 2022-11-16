package lv.rtu.autograderserver.model;


import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "tasks")
public class Task implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Embedded
    private AuditMetadata auditMetadata;

    public Task() {

    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public AuditMetadata getAudit() {
        return auditMetadata;
    }

    @Override
    public void setAudit(AuditMetadata auditMetadata) {
        this.auditMetadata = auditMetadata;
    }


}
