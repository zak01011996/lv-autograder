package lv.rtu.autograderserver.repository;

import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    @Query("SELECT p FROM Publication p WHERE p.publicId = ?1 AND p.availableFrom <= current_timestamp AND p.availableTo >= current_timestamp")
    Optional<Publication> fetchOngoingPublication(String publicId);
}
