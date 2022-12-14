package lv.rtu.autograderserver.ui.view.manager.usermanagement;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.service.UserService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.component.form.PasswordResetForm;
import lv.rtu.autograderserver.ui.component.form.UserForm;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

@RolesAllowed("ROLE_ADMIN")
@Route(value = "manager/users", layout = MainLayout.class)
public class UsersManagementView extends VerticalLayout implements HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(UsersManagementView.class);

    private UserService userService;

    private Grid<User> grid = new Grid<>();

    public UsersManagementView(@NotNull UserService userService) {
        this.userService = userService;

        createHeading();
        createGrid();

        updateGridData();
    }

    private void createHeading() {
        H2 title = new H2(getTranslation("user_management_title"));
        add(title);

        Button addUserBtn = new Button(getTranslation("user_management_btn_add_user"), VaadinIcon.PLUS.create());
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserBtn.addClickListener(event -> showUserForm(new User(), false));

        add(addUserBtn);
    }

    private void showUserForm(@NotNull User user, boolean isEditing) {
        Dialog dialog = new Dialog();
        dialog.setWidth(30, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        UserForm form = new UserForm(user, isEditing);
        form.registerSaveCallback(data -> {
            try {
                userService.saveUser(data);
                updateGridData();

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

    private void showPasswordResetForm(@NotNull User user) {
        Dialog dialog = new Dialog();
        dialog.setWidth(30, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        PasswordResetForm form = new PasswordResetForm(user);
        form.registerSaveCallback(data -> {
            try {
                userService.resetPassword(data.getId(), data.getPassword());
                updateGridData();

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

    private void createGrid() {
        grid.setWidthFull();
        grid.addColumn(User::getId)
                .setHeader(getTranslation("user_management_grid_id"))
                .setFlexGrow(0)
                .setWidth("5em");
        grid.addColumn(User::getEmail)
                .setHeader(getTranslation("user_management_grid_email"))
                .setFlexGrow(0)
                .setWidth("17em");

        grid.addColumn(User::getFirstName)
                .setHeader(getTranslation("user_management_grid_first_name"))
                .setFlexGrow(0)
                .setWidth("17em");

        grid.addColumn(User::getLastName)
                .setHeader(getTranslation("user_management_grid_last_name"))
                .setFlexGrow(0)
                .setWidth("17em");

        grid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getCreatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("user_management_grid_created_at"))
                .setFlexGrow(0)
                .setWidth("15em");

        grid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getUpdatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("user_management_grid_updated_at"))
                .setFlexGrow(0)
                .setWidth("15em");

        grid.addColumn(new ComponentRenderer<>(user -> {
            Icon res = VaadinIcon.CHECK_CIRCLE.create();
            res.setColor("green");
            if (!user.isActive()) {
                res = VaadinIcon.BAN.create();
                res.setColor("red");
            }

            return res;
        })).setHeader(getTranslation("user_management_grid_is_active"))
                .setFlexGrow(0)
                .setWidth("7em");

        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button edit = new Button(getTranslation("user_management_grid_btn_edit"), VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL);
            edit.addClickListener(event -> showUserForm(user, true));

            Button resetPassword = new Button(
                    getTranslation("user_management_grid_btn_reset_password"), VaadinIcon.PASSWORD.create());
            resetPassword.addThemeVariants(ButtonVariant.LUMO_SMALL);
            resetPassword.addClickListener(event -> showPasswordResetForm(user));

            Button setEnabled = new Button(getTranslation("user_management_grid_btn_block"), VaadinIcon.BAN.create());
            setEnabled.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            if (!user.isActive()) {
                setEnabled.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
                setEnabled.setText(getTranslation("user_management_grid_btn_activate"));
                setEnabled.removeThemeVariants(ButtonVariant.LUMO_ERROR);
                setEnabled.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            }
            setEnabled.addClickListener(event -> {
                try {
                    userService.toggleActive(user.getId());
                    updateGridData();
                } catch (Exception ex) {
                    logger.error("Cannot save user: ", ex);
                }
            });

            layout.add(edit, resetPassword, setEnabled);
            return layout;
        })).setHeader(getTranslation("user_management_grid_actions"));

        add(grid);
    }

    private void updateGridData() {
        grid.setItems(userService.fetchAll());
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_manager_user");
    }
}
