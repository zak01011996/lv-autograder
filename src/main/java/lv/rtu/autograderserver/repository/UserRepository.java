package lv.rtu.autograderserver.repository;

import lv.rtu.autograderserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(@NotNull String email);
    List<User> findAllByOrderByIdAsc();
}
