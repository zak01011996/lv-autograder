package lv.rtu.autograderserver.ui.view.manager.settings;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.security.LoggedInUser;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.UserService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.component.form.PasswordResetForm;
import lv.rtu.autograderserver.ui.component.form.UserForm;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;

@PermitAll
@Route(value = "manager/settings", layout = MainLayout.class)
public class SettingsView extends VerticalLayout implements HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecurityService securityService;
    private final UserService userService;

    public SettingsView(@NotNull SecurityService securityService, @NotNull UserService userService) {
        this.securityService = securityService;
        this.userService = userService;
        add(new H2(getTranslation("settings_title")));
        createUserInfoPanel();
    }

    private void createUserInfoPanel() {
        LoggedInUser userInfo = securityService.getAuthenticatedUser();

        HorizontalLayout panel = new HorizontalLayout();
        panel.setWidth(50, Unit.EM);
        panel.addClassNames("bordered");


        VerticalLayout userInfoLayout = new VerticalLayout();
        H3 userInfoTitle = new H3(getTranslation("settings_user_info_title"));
        userInfoTitle.addClassNames("block_title");
        Span email = new Span(getTranslation("settings_info_email", userInfo.getUsername()));
        Span firstName = new Span(getTranslation("settings_info_first_name", userInfo.getFirstName()));
        Span lastName = new Span(getTranslation("settings_info_last_name", userInfo.getLastName()));
        userInfoLayout.add(userInfoTitle, email, firstName, lastName);

        VerticalLayout actionsLayout = new VerticalLayout();
        H3 actionsTitle = new H3(getTranslation("settings_actions_title"));
        actionsTitle.addClassNames("block_title");
        Button editBtn = new Button(getTranslation("settings_info_btn_edit"), VaadinIcon.EDIT.create());
        editBtn.setWidthFull();
        editBtn.addClickListener(event -> showUserForm());

        Button changePassBtn = new Button(getTranslation("settings_info_btn_change_pass"), VaadinIcon.PASSWORD.create());
        changePassBtn.setWidthFull();
        changePassBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        changePassBtn.addClickListener(event -> showPasswordResetForm());

        actionsLayout.add(actionsTitle);
        if (!userInfo.isAdmin()) {
            actionsLayout.add(editBtn, changePassBtn);
        } else {
            Span noActionsMsg = new Span(getTranslation("settings_info_no_actions_msg"));
            noActionsMsg.getStyle().set("color", "gray");
            actionsLayout.add(noActionsMsg);
        }

        panel.add(userInfoLayout, actionsLayout);

        add(panel);
    }

    private void showUserForm() {
        User userData = userService.fetchUserById(securityService.getAuthenticatedUser().getId())
                .orElseThrow(() -> new RuntimeException("Cannot find user"));

        Dialog dialog = new Dialog();
        dialog.setWidth(30, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        UserForm form = new UserForm(userData, true);
        form.registerSaveCallback(data -> {
            try {
                userService.saveUser(data);
                securityService.updateUserData(data.getEmail(), data.getFirstName(), data.getLastName());

                NotificationHelper.displaySuccess(getTranslation("user_form_message_success"));
                dialog.close();

                UI.getCurrent().getPage().reload();
            } catch (Exception exception) {
                NotificationHelper.displayError(getTranslation("user_form_message_error"));
                logger.error("Cannot save user: ", exception);
            }
        });

        form.registerCancelCallback(data -> dialog.close());

        dialog.add(form);
        dialog.open();
    }

    private void showPasswordResetForm() {
        User userData = userService.fetchUserById(securityService.getAuthenticatedUser().getId())
                .orElseThrow(() -> new RuntimeException("Cannot find user"));

        Dialog dialog = new Dialog();
        dialog.setWidth(30, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        PasswordResetForm form = new PasswordResetForm(userData);
        form.registerSaveCallback(data -> {
            try {
                userService.resetPassword(data.getId(), data.getPassword());

                NotificationHelper.displaySuccess(getTranslation("user_form_message_success"));
                dialog.close();
            } catch (Exception exception) {
                NotificationHelper.displayError(getTranslation("user_form_message_error"));
                logger.error("Cannot save user: ", exception);
            }
        });

        form.registerCancelCallback(data -> dialog.close());

        dialog.add(form);
        dialog.open();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_manager_settings");
    }
}
