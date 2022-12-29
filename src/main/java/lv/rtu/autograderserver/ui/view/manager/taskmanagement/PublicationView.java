package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;

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

        // Empty label to expand it
        Label emptySpace = new Label();
        btnLayout.add(backBtn, emptySpace);
        btnLayout.expand(emptySpace);

        add(title, btnLayout);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_publication", taskData.getTitle());
    }
}
