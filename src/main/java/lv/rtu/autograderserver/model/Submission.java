package lv.rtu.autograderserver.model;

import lv.rtu.autograderserver.config.AuditListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "submissions")
public class Submission implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "score")
    private int score;

    @Column(name = "maxScore")
    private int maxScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(name = "solution_content", columnDefinition = "TEXT")
    private String solutionContent;

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
