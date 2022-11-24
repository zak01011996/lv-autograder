package lv.rtu.autograderserver.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Author notes:
 * Code was taken and modified from official documentation
 * Source: https://vaadin.com/docs/latest/tutorial/login-and-authentication
 */
@Component
public class SecurityService {
    private static final String LOGOUT_SUCCESS_URL = "/manager";

    public LoggedInUser getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof LoggedInUser) {
            return (LoggedInUser) principal;
        }

        // Anonymous or no authentication.
        return null;
    }

    public void updateUserData(@NotNull String email, @NotNull String firstName, @NotNull String lastName) {
        LoggedInUser user = getAuthenticatedUser();
        if (user == null) {
            return;
        }

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                null
        );
    }
}
