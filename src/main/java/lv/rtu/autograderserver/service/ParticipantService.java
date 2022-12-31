package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.*;
import lv.rtu.autograderserver.repository.ParticipantRepository;
import lv.rtu.autograderserver.repository.PublicationRepository;
import lv.rtu.autograderserver.ui.component.form.ParticipantRegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ParticipantService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ParticipantRepository participantRepository;

    public ParticipantService(@NotNull ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public Participant createNewParticipant(@NotNull Participant participant, @NotNull Publication publication) {
        participant.setPublication(publication);

        for (Problem problem : publication.getTask().getProblems()) {
            Submission submission = new Submission();
            submission.setParticipant(participant);
            submission.setProblem(problem);
            submission.setSolutionContent(
                    problem
                            .getFiles()
                            .stream()
                            .filter(ProblemFile::isSolutionTemplate)
                            .map(ProblemFile::getContent)
                            .findFirst()
                            .orElse(null)
            );

            participant.getSubmissions().add(submission);
        }

        return participantRepository.save(participant);
    }

    public Participant saveParticipantData(@NotNull Participant participant) {
        return participantRepository.save(participant);
    }

    public Participant prepareSubmissionsForProcessing(@NotNull Participant participant) {
        participant.setSubmittedAt(LocalDateTime.now());
        participant.getSubmissions().forEach(s -> s.setStatus(SubmissionStatus.SUBMITTED));
        return saveParticipantData(participant);
    }
}
