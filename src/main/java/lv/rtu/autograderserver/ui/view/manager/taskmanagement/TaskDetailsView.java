package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.*;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.component.form.NewProblemForm;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@PermitAll
@Route(value = "manager/task/:taskId", layout = MainLayout.class)
public class TaskDetailsView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Grid<Problem> problemGrid = new Grid<>();
    private final Grid<Publication> publicationGrid = new Grid<>();

    private final TaskManagementShared shared;
    private final SecurityService securityService;
    private final TaskService taskService;
    private Task taskData;

    private VerticalLayout mainContent;
    private VerticalLayout tabContent;

    public TaskDetailsView(
            @NotNull TaskService taskService,
            @NotNull SecurityService securityService,
            @NotNull TaskManagementShared shared
    ) {
        this.securityService = securityService;
        this.taskService = taskService;
        this.shared = shared;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            long taskId = Long.parseLong(event.getRouteParameters().get("taskId").orElseThrow(() ->
                    new RuntimeException("TaskID not provided")));

            taskData = taskService.fetchByTaskIdAndUserId(taskId, securityService.getAuthenticatedUser().getId())
                    .orElseThrow(() -> new RuntimeException("No task found for given user"));
        } catch (Exception ex) {
            logger.error("Got an error during initialization task view", ex);
            event.forwardTo(TaskListView.class);
            return;
        }

        mainContent = new VerticalLayout();
        add(mainContent);

        fillMainContent();
    }

    private void fillMainContent() {
        mainContent.removeAll();

        H2 title = new H2(taskData.getTitle());

        // Buttons block
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setWidthFull();
        btnLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button backBtn = new Button(getTranslation("go_back"), VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(event -> UI.getCurrent().navigate(TaskListView.class));

        Button editTaskDetails = new Button(getTranslation("task_details_btn_edit"), VaadinIcon.EDIT.create());
        editTaskDetails.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        editTaskDetails.addClickListener(event -> shared.showTaskForm(taskData, null, task -> {
            taskData = task;
            fillMainContent();
        }));

        Button deleteTaskBtn = new Button(getTranslation("task_details_btn_delete"), VaadinIcon.TRASH.create());
        deleteTaskBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteTaskBtn.addClickListener(event ->
                shared.showDeleteTaskDialog(taskData, () -> {
                    UI.getCurrent().navigate(TaskListView.class);
                    return null;
                })
        );

        // Empty label to expand it
        Label emptySpace = new Label();
        btnLayout.add(backBtn, emptySpace, editTaskDetails, deleteTaskBtn);
        btnLayout.expand(emptySpace);

        // Task other properties
        Tab description = new Tab(getTranslation("task_details_tab_description"));
        Tab problems = new Tab(getTranslation("task_details_tab_problems"));
        Tab publications = new Tab(getTranslation("task_details_tab_publications"));

        tabContent = new VerticalLayout();

        Tabs tabs = new Tabs(description, problems, publications);
        tabs.setWidthFull();
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(description)) {
                fillDescriptionContent();
            } else if (event.getSelectedTab().equals(problems)) {
                fillProblemsContent();
            } else if (event.getSelectedTab().equals(publications)) {
                fillPublicationsContent();
            }
        });
        tabs.setSelectedTab(description);
        fillDescriptionContent();

        mainContent.add(title, btnLayout, tabs, tabContent);
    }

    private void fillDescriptionContent() {
        tabContent.removeAll();
        tabContent.add(new Html(String.format("<div>%s</div>", taskData.getDescription())));
    }

    private void fillProblemsContent() {
        tabContent.removeAll();
        problemGrid.removeAllColumns();

        VerticalLayout layout = new VerticalLayout();

        NewProblemForm form = new NewProblemForm();
        form.registerSaveCallback(problem -> {
            try {
                taskData = taskService.createNewProblem(taskData, problem);
                problemGrid.setItems(taskData.getProblems());
                form.reset();

                NotificationHelper.displaySuccess(getTranslation("problem_form_message_success"));
            } catch (Exception ex) {
                logger.error("Got error during saving problem", ex);
                NotificationHelper.displayError(getTranslation("problem_form_message_error"));
            }
        });

        Details newProblemForm = new Details(getTranslation("task_details_problem_btn_create"), form);
        newProblemForm.setWidthFull();
        newProblemForm.setOpened(true);

        layout.add(newProblemForm);

        problemGrid.addColumn(Problem::getId)
                .setHeader(getTranslation("task_details_problem_grid_id"))
                .setFlexGrow(0)
                .setWidth("5em");

        problemGrid.addColumn(Problem::getTitle).setHeader(getTranslation("task_details_problem_grid_title"));
        problemGrid.addColumn(Problem::getSandboxType).setHeader(getTranslation("task_details_problem_grid_sandbox_type"));
        problemGrid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getCreatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_details_problem_grid_created_at"));

        problemGrid.addColumn(new LocalDateTimeRenderer<>(u -> u.getAudit().getUpdatedAt(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_details_problem_grid_update_at"));

        problemGrid.addColumn(new ComponentRenderer<>(problem -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            Button configureBtn = new Button(
                    getTranslation("task_details_problem_grid_action_configure"), VaadinIcon.COG.create());

            configureBtn.addClickListener(event -> navigateToProblemConfiguration(problem.getId()));

            actionsLayout.add(configureBtn);

            return actionsLayout;
        })).setHeader(getTranslation("task_details_problem_grid_actions"));

        problemGrid.setItems(taskData.getProblems());

        layout.add(problemGrid);

        tabContent.add(layout);
    }

    private void fillPublicationsContent() {
        tabContent.removeAll();
        publicationGrid.removeAllColumns();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_task_details", taskData.getTitle());
    }

    private void navigateToProblemConfiguration(long problemId) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskData.getId()));
        params.put("problemId", String.valueOf(problemId));

        UI.getCurrent().navigate(ProblemConfigurationView.class, new RouteParameters(params));
    }
}
