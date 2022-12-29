package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "publications")
public class Publication implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "publication_id", unique = true)
    private String publicId = UUID.randomUUID().toString();

    @Column(name = "password")
    private String password;

    @Column(name = "avaialable_from")
    private LocalDateTime availableFrom;

    @Column(name = "avaialable_to")
    private LocalDateTime availableTo;

    @Column(name = "time_limit")
    private int timeLimit;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Embedded
    private AuditMetadata auditMetadata;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDateTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalDateTime getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalDateTime availableTo) {
        this.availableTo = availableTo;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public AuditMetadata getAudit() {
        return auditMetadata;
    }

    @Override
    public void setAudit(AuditMetadata metadata) {
        this.auditMetadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Publication that = (Publication) o;
        return id == that.id && timeLimit == that.timeLimit && Objects.equals(publicId, that.publicId) && Objects.equals(password, that.password) && Objects.equals(availableFrom, that.availableFrom) && Objects.equals(availableTo, that.availableTo) && Objects.equals(participants, that.participants) && Objects.equals(task, that.task) && Objects.equals(auditMetadata, that.auditMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, password, availableFrom, availableTo, timeLimit, participants, task, auditMetadata);
    }
}
