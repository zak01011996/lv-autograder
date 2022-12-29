package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
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
import lv.rtu.autograderserver.ui.component.form.NewPublicationForm;
import lv.rtu.autograderserver.ui.component.form.TaskForm;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@JsModule("copytoclipboard.js")
@PermitAll
@Route(value = "manager/task/:taskId", layout = MainLayout.class)
public class TaskDetailsView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String solutionUrl;

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
            @NotNull TaskManagementShared shared,
            @Value("${app.solution_url}") String solutionUrl
    ) {
        this.securityService = securityService;
        this.taskService = taskService;
        this.shared = shared;

        this.solutionUrl = solutionUrl;
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
        btnLayout.add(backBtn, emptySpace, deleteTaskBtn);
        btnLayout.expand(emptySpace);

        // Task other properties
        Tab details = new Tab(getTranslation("task_details_tab_details"));
        Tab problems = new Tab(getTranslation("task_details_tab_problems"));
        Tab publications = new Tab(getTranslation("task_details_tab_publications"));

        tabContent = new VerticalLayout();

        Tabs tabs = new Tabs(details, problems, publications);
        tabs.setWidthFull();
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(details)) {
                fillTaskDetails();
            } else if (event.getSelectedTab().equals(problems)) {
                fillProblemsContent();
            } else if (event.getSelectedTab().equals(publications)) {
                fillPublicationsContent();
            }
        });
        tabs.setSelectedTab(details);
        fillTaskDetails();

        mainContent.add(title, btnLayout, tabs, tabContent);
    }

    private void fillTaskDetails() {
        tabContent.removeAll();

        TaskForm form = new TaskForm(taskData, false, false);
        form.registerSaveCallback(data -> {
            try {
                taskData = taskService.saveTask(data);
                NotificationHelper.displaySuccess(getTranslation("task_form_message_success"));
            } catch (Exception exception) {
                NotificationHelper.displayError(getTranslation("task_form_message_error"));
                logger.error("Cannot save task: ", exception);
            }
        });

        tabContent.add(form);
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
            configureBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
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

        VerticalLayout layout = new VerticalLayout();

        NewPublicationForm form = new NewPublicationForm();
        form.setWidthFull();
        form.registerSaveCallback(publication -> {
            try {
                taskData = taskService.createNewPublication(taskData, publication);
                publicationGrid.setItems(taskData.getPublications());
                form.reset();

                NotificationHelper.displaySuccess(getTranslation("publication_form_message_success"));
            } catch (Exception ex) {
                logger.error("Got error during saving publication", ex);
                NotificationHelper.displayError(getTranslation("publication_form_message_error"));
            }
        });

        layout.add(form);

        publicationGrid.addColumn(Publication::getId)
                .setHeader(getTranslation("task_details_publication_grid_id"))
                .setFlexGrow(0)
                .setWidth("5em");

        publicationGrid.addColumn(Publication::getPublicId)
                .setHeader(getTranslation("task_details_publication_grid_public_id"));

        publicationGrid.addColumn(new ComponentRenderer<>(this::getPublicationStatus))
                .setHeader(getTranslation("task_details_publication_grid_status"));

        publicationGrid.addColumn(new LocalDateTimeRenderer<>(Publication::getAvailableFrom, "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_details_publication_grid_available_from"));

        publicationGrid.addColumn(new LocalDateTimeRenderer<>(Publication::getAvailableTo, "yyyy-MM-dd HH:mm:ss"))
                .setHeader(getTranslation("task_details_publication_grid_available_to"));

        publicationGrid.addColumn(new ComponentRenderer<>(publication -> {
            if (Strings.isEmpty(publication.getPassword())) {
                return VaadinIcon.BAN.create();
            }

            return VaadinIcon.LOCK.create();
        })).setHeader(getTranslation("task_details_publication_grid_is_secured"));

        publicationGrid.addColumn(new ComponentRenderer<>(publication -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            Button viewBtn = new Button(
                    getTranslation("task_details_publication_grid_action_view"), VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewBtn.addClickListener(event -> navigateToPublicationPage(publication.getId()));

            Button copyLink = new Button(
                    getTranslation("task_details_publication_grid_action_copy_link"), VaadinIcon.COPY_O.create());
            copyLink.addThemeVariants(ButtonVariant.LUMO_SMALL);
            copyLink.addClickListener(event -> {
                UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)",
                        solutionUrl.replace("{0}", publication.getPublicId()));
                NotificationHelper.displaySuccess(getTranslation("task_details_publication_msg_link_copied"));
            });

            actionsLayout.add(viewBtn, copyLink);

            return actionsLayout;
        })).setHeader(getTranslation("task_details_publication_grid_actions"));

        publicationGrid.setItems(taskData.getPublications());

        layout.add(publicationGrid);
        tabContent.add(layout);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_task_details", taskData.getTitle());
    }

    private Label getPublicationStatus(Publication publication) {
        Label res = new Label();
        LocalDateTime currentTime = LocalDateTime.now();

        if (publication.getAvailableFrom().isAfter(currentTime)) {
            res.setText(getTranslation("task_details_publication_status_pending"));
            res.getStyle().set("color", "orange");
        } else if (publication.getAvailableFrom().isBefore(currentTime)
                && publication.getAvailableTo().isAfter(currentTime)) {
            res.setText(getTranslation("task_details_publication_status_ongoing"));
            res.getStyle().set("color", "green");
        } else {
            res.setText(getTranslation("task_details_publication_status_expired"));
            res.getStyle().set("color", "grey");
        }

        return res;
    }

    private void navigateToProblemConfiguration(long problemId) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskData.getId()));
        params.put("problemId", String.valueOf(problemId));

        UI.getCurrent().navigate(ProblemConfigurationView.class, new RouteParameters(params));
    }

    private void navigateToPublicationPage(long problemId) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskData.getId()));
        params.put("publicationId", String.valueOf(problemId));

        UI.getCurrent().navigate(PublicationView.class, new RouteParameters(params));
    }
}
