package lv.rtu.autograderserver.repository;

import lv.rtu.autograderserver.model.Participant;
import lv.rtu.autograderserver.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
