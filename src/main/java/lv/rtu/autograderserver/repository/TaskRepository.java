package lv.rtu.autograderserver.repository;

import lv.rtu.autograderserver.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.auditMetadata.createdBy = ?1")
    List<Task> findAllByUserBy(long userId);

    @Query("SELECT t FROM Task t WHERE t.id = ?1 AND t.auditMetadata.createdBy = ?2")
    Optional<Task> findTaskByIdAndUserId(long id, long userId);
}
