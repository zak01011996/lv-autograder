package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;
import java.util.Objects;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "problem_files")
public class ProblemFile implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_solution_template")
    private boolean isSolutionTemplate = false;

    @Column(name = "is_container_entrypoint")
    private boolean isContainerEntryPoint = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Embedded
    private AuditMetadata auditMetadata;

    public ProblemFile() {

    }

    public ProblemFile(Problem problem) {
        this.problem = problem;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSolutionTemplate() {
        return isSolutionTemplate;
    }

    public void setSolutionTemplate(boolean solutionTemplate) {
        isSolutionTemplate = solutionTemplate;
    }

    public boolean isContainerEntryPoint() {
        return isContainerEntryPoint;
    }

    public void setContainerEntryPoint(boolean containerEntryPoint) {
        isContainerEntryPoint = containerEntryPoint;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
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
        ProblemFile that = (ProblemFile) o;
        return id == that.id && isSolutionTemplate == that.isSolutionTemplate && isContainerEntryPoint == that.isContainerEntryPoint && Objects.equals(fileName, that.fileName) && Objects.equals(content, that.content) && Objects.equals(problem, that.problem) && Objects.equals(auditMetadata, that.auditMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, content, isSolutionTemplate, isContainerEntryPoint, problem, auditMetadata);
    }
}
