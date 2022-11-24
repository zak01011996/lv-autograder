package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

@PermitAll
@Route(value = "manager", layout = MainLayout.class)
public class TaskListView extends VerticalLayout implements HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TaskListView() {
        add(new Label("TEST CONTENT"), new Label("TEST CONTENT 2"));
    }

    private void createGrid() {
//        grid.setWidthFull();
//        grid.addColumn(User::getId)
//                .setHeader(getTranslation("user_management_grid_id"))
//                .setFlexGrow(0)
//                .setWidth("5em");
//        grid.addColumn(User::getEmail)
//                .setHeader(getTranslation("user_management_grid_email"))
//                .setFlexGrow(0)
//                .setWidth("17em");
//
//        grid.addColumn(User::getFirstName)
//                .setHeader(getTranslation("user_management_grid_first_name"))
//                .setFlexGrow(0)
//                .setWidth("17em");
//
//        grid.addColumn(User::getLastName)
//                .setHeader(getTranslation("user_management_grid_last_name"))
//                .setFlexGrow(0)
//                .setWidth("17em");
//
//        grid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getCreatedAt(), "yyyy-MM-dd HH:mm:ss"))
//                .setHeader(getTranslation("user_management_grid_created_at"))
//                .setFlexGrow(0)
//                .setWidth("15em");
//
//        grid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getUpdatedAt(), "yyyy-MM-dd HH:mm:ss"))
//                .setHeader(getTranslation("user_management_grid_updated_at"))
//                .setFlexGrow(0)
//                .setWidth("15em");
//
//        grid.addColumn(new ComponentRenderer<>(user -> {
//                    Icon res = VaadinIcon.CHECK_CIRCLE.create();
//                    res.setColor("green");
//                    if (!user.isActive()) {
//                        res = VaadinIcon.BAN.create();
//                        res.setColor("red");
//                    }
//
//                    return res;
//                })).setHeader(getTranslation("user_management_grid_isactive"))
//                .setFlexGrow(0)
//                .setWidth("7em");
//
//        grid.addColumn(new ComponentRenderer<>(user -> {
//            HorizontalLayout layout = new HorizontalLayout();
//
//            Button edit = new Button(getTranslation("user_management_grid_btn_edit"), VaadinIcon.EDIT.create());
//            edit.addThemeVariants(ButtonVariant.LUMO_SMALL);
//            edit.addClickListener(event -> showUserForm(user, true));
//
//            Button resetPassword = new Button(
//                    getTranslation("user_management_grid_btn_reset_password"), VaadinIcon.PASSWORD.create());
//            resetPassword.addThemeVariants(ButtonVariant.LUMO_SMALL);
//            resetPassword.addClickListener(event -> showPasswordResetForm(user));
//
//            Button setEnabled = new Button(getTranslation("user_management_grid_btn_block"), VaadinIcon.BAN.create());
//            setEnabled.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
//            if (!user.isActive()) {
//                setEnabled.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
//                setEnabled.setText(getTranslation("user_management_grid_btn_activate"));
//                setEnabled.removeThemeVariants(ButtonVariant.LUMO_ERROR);
//                setEnabled.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
//            }
//            setEnabled.addClickListener(event -> {
//                try {
//                    userService.toggleActive(user.getId());
//                    updateGridData();
//                } catch (Exception ex) {
//                    logger.error("Cannot save user: ", ex);
//                }
//            });
//
//            layout.add(edit, resetPassword, setEnabled);
//            return layout;
//        })).setHeader(getTranslation("user_management_grid_actions"));
//
//        add(grid);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_manager_tasks");
    }
}
