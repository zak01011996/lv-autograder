package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.service.UserService;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import java.util.List;

@PermitAll
@Route(value = "manager", layout = MainLayout.class)
public class TaskListView extends VerticalLayout implements HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecurityService securityService;
    private final UserService userService;
    private final TaskService taskService;
    private final TaskManagementShared shared;

    private User selectedUser;
    private VerticalLayout mainContent;
    private Grid<Task> taskGrid = new Grid<>();

    public TaskListView(
            @NotNull SecurityService securityService,
            @NotNull UserService userService,
            @NotNull TaskService taskService,
            @NotNull TaskManagementShared shared
    ) {
        this.securityService = securityService;
        this.userService = userService;
        this.taskService = taskService;
        this.shared = shared;

        H2 title = new H2(getTranslation("task_management_title"));
        add(title);

        // Superuser is able to edit all tasks by any user
        if (securityService.isAdmin()) {
            List<User> userData = userService.fetchAll();
            ComboBox<User> users = new ComboBox<>("Select user");
            users.setWidthFull();
            users.setItemLabelGenerator(user ->
                    String.format("ID: %d, %s %s (%s)", user.getId(), user.getFirstName(), user.getLastName(), user.getEmail())
            );
            users.addValueChangeListener(event -> {
                this.selectedUser = event.getValue();
                createMainContent();
                updateGridData();
            });
            users.setItems(userData);
            users.setPlaceholder("Type to find user...");

            add(users);
        } else {
            // If it's not superuser then selected user will be current user from session
            selectedUser = userService.fetchUserById(securityService.getAuthenticatedUser().getId())
                    .orElseThrow(() -> new RuntimeException("Not authorized"));

            createMainContent();
            updateGridData();
        }
    }

    private void createMainContent() {
        if (mainContent == null) {
            mainContent = new VerticalLayout();
            Button createNewTask = new Button(getTranslation("task_management_btn_create"), VaadinIcon.PLUS.create());
            createNewTask.addClickListener(event ->
                    shared.showTaskForm(new Task(), selectedUser, res -> updateGridData())
            );

            mainContent.add(createNewTask);
            createGrid();

            add(mainContent);
        }
    }

    private void createGrid() {
        taskGrid.setWidthFull();
        taskGrid.addColumn(Task::getId)
                .setHeader(getTranslation("task_management_grid_id"))
                .setFlexGrow(0)
                .setWidth("5em");

        taskGrid.addColumn(Task::getTitle).setHeader(getTranslation("task_management_grid_title"));

        taskGrid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getCreatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_management_grid_created_at"));

        taskGrid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getUpdatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_management_grid_updated_at"));

        taskGrid.addColumn(new ComponentRenderer<>(task -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button viewBtn = new Button(getTranslation("task_management_grid_btn_view"), VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewBtn.addClickListener(event -> UI.getCurrent().navigate(
                    TaskDetailsView.class, new RouteParameters("taskId", String.valueOf(task.getId()))));

            Button deleteBtn = new Button(getTranslation("task_management_grid_btn_delete"), VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(event -> shared.showDeleteTaskDialog(task, () -> {
                updateGridData();
                return null;
            }));

            layout.add(viewBtn, deleteBtn);
            return layout;
        })).setHeader(getTranslation("task_management_grid_actions"));

        mainContent.add(taskGrid);
    }

    private void updateGridData() {
        taskGrid.setItems(taskService.fetchAllByUserId(selectedUser.getId()));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_manager_tasks");
    }
}
