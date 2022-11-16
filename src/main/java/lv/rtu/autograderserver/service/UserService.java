package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    // TODO: Not good solution, but for users list it's OK
    public List<User> fetchAll() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public UserService(@NotNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> fetchUserById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> fetchUserByEmail(@NotNull String email) {
        return userRepository.findUserByEmail(email);
    }

    public User saveUser(@NotNull User user) {
        return userRepository.save(user);
    }

    public User resetPassword(long id, @NotNull String newPass) {
        User user = fetchUserById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPassword(newPass);
        return saveUser(user);
    }

    public User toggleActive(long id) {
        User user = fetchUserById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setActive(!user.isActive());
        return saveUser(user);
    }
}
