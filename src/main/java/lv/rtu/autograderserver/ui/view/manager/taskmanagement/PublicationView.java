package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Submission;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;

@PermitAll
@Route(value = "manager/task/:taskId/publication/:publicationId", layout = MainLayout.class)
public class PublicationView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecurityService securityService;
    private final TaskService taskService;

    private Task taskData;
    private Publication publicationData;

    public PublicationView(@NotNull TaskService taskService, @NotNull SecurityService securityService) {
        this.securityService = securityService;
        this.taskService = taskService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            long taskId = Long.parseLong(event.getRouteParameters().get("taskId").orElseThrow(() ->
                    new RuntimeException("TaskID param not provided")));

            long publicationId = Long.parseLong(event.getRouteParameters().get("publicationId").orElseThrow(() ->
                    new RuntimeException("ProblemID param not provided")));

            taskData = taskService.fetchByTaskIdAndUserId(taskId, securityService.getAuthenticatedUser().getId())
                    .orElseThrow(() -> new RuntimeException("No task found for given user"));

            publicationData = taskData.getPublications().stream().filter(
                    p -> p.getId() == publicationId).findFirst().orElseThrow();

        } catch (Exception ex) {
            logger.error("Got an error during initialization task view", ex);
            event.forwardTo(TaskListView.class);
            return;
        }

        createMainContent();
    }

    private void createMainContent() {
        H2 title = new H2(getTranslation("publication_view_title", taskData.getTitle()));

        // Buttons block
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setWidthFull();
        btnLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        Button backBtn = new Button(getTranslation("go_back"), VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(event -> UI.getCurrent().navigate(TaskDetailsView.class,
                new RouteParameters("taskId", String.valueOf(taskData.getId()))));

        Button exportResultBtn = new Button(getTranslation("publication_view_btn_export_to_moodle"),
                VaadinIcon.FILE_TABLE.create());
        exportResultBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        exportResultBtn.addClickListener(event -> {});


        // Empty label to expand it
        Label emptySpace = new Label();
        btnLayout.add(backBtn, emptySpace, exportResultBtn);
        btnLayout.expand(emptySpace);

        VerticalLayout infoBlock = createInfoBlock();
        VerticalLayout groupedStatisticsBlock = createGroupedStatisticsBlock();
        VerticalLayout overallStatisticsBlock = createOverallStatisticsBlock();

        add(title, btnLayout, infoBlock, groupedStatisticsBlock, overallStatisticsBlock);
    }

    private VerticalLayout createInfoBlock() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addClassNames("bordered");

        H3 title = new H3(getTranslation("publication_view_info_title"));
        title.addClassNames("block_title");

        Span publicationId = new Span(getTranslation("publication_view_public_id", publicationData.getPublicId()));
        Span availableFrom = new Span(getTranslation("publication_view_available_from", publicationData.getAvailableFrom()));
        Span availableTo = new Span(getTranslation("publication_view_available_to", publicationData.getAvailableTo()));

        layout.add(title, publicationId, availableFrom, availableTo);

        if (!Strings.isEmpty(publicationData.getPassword())) {
            Span password = new Span(getTranslation("publication_view_password", publicationData.getPassword()));
            layout.add(password);
        }

        return layout;
    }

    private VerticalLayout createGroupedStatisticsBlock() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();

        H2 title = new H2(getTranslation("publication_view_grouped_statistics_title"));
        layout.add(title);

        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();

        // Grid with submission results
        Grid<Submission> submissionGrid = new Grid<>();
        submissionGrid.setWidth(85, Unit.PERCENTAGE);
        submissionGrid.removeAllColumns();

        // Problem list
        Map<Tab, Problem> problemMap = createProblemTabs();
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setWidth(15, Unit.PERCENTAGE);
        tabs.addSelectedChangeListener(event -> {});
        problemMap.forEach((k, v) -> tabs.add(k));

        contentLayout.add(tabs, submissionGrid);

        layout.add(contentLayout);
        return layout;
    }

    private VerticalLayout createOverallStatisticsBlock() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();

        H2 title = new H2(getTranslation("publication_view_all_statistics_title"));
        layout.add(title);

        return layout;
    }

    private Map<Tab, Problem> createProblemTabs() {
        return taskData.getProblems().stream().collect(Collectors.toMap(p -> new Tab(p.getTitle()), p -> p));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_publication", taskData.getTitle());
    }
}
