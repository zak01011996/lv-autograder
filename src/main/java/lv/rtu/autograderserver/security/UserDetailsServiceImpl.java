package lv.rtu.autograderserver.security;

import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserService userService;

    private final String adminUsername;
    private final String adminPassword;

    public UserDetailsServiceImpl(
            @NotNull UserService userService,
            @NotNull @Value("${app.superuser.username}") String adminUsername,
            @NotNull @Value("${app.superuser.password}") String adminPassword
    ) {
        this.userService = userService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Login attempt: {}", username);
        if (adminUsername.equals(username)) {
            return fetchSuperuser();
        }

        User user = userService
                .fetchUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        return new LoggedInUser(user);
    }

    private LoggedInUser fetchSuperuser() {
        return new LoggedInUser(
                0,
                adminUsername,
                adminPassword,
                "Admin",
                "",
                true,
                List.of("ROLE_ADMIN", "ROLE_USER")
        );
    }
}
