package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "problems")
public class Problem implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sandbox_type")
    @Enumerated(EnumType.STRING)
    private SandboxType sandboxType;

    @Column(name = "max_score")
    private int maxScore = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemFile> files = new ArrayList<>();

    @Embedded
    private AuditMetadata auditMetadata;

    public Problem() {

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

    public SandboxType getSandboxType() {
        return sandboxType;
    }

    public void setSandboxType(SandboxType sandboxType) {
        this.sandboxType = sandboxType;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<ProblemFile> getFiles() {
        return files;
    }

    public void setFiles(List<ProblemFile> files) {
        this.files = files;
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
        Problem problem = (Problem) o;
        return id == problem.id && Objects.equals(title, problem.title) && Objects.equals(description, problem.description) && sandboxType == problem.sandboxType && Objects.equals(task, problem.task) && Objects.equals(files, problem.files) && Objects.equals(auditMetadata, problem.auditMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, sandboxType, task, files, auditMetadata);
    }

    @Override
    public String toString() {
        return "Problem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", sandboxType=" + sandboxType +
                ", files=" + files +
                ", auditMetadata=" + auditMetadata +
                '}';
    }
}
