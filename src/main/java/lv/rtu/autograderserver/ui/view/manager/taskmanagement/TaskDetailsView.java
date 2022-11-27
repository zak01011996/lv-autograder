package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
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
@Route(value = "manager/task", layout = MainLayout.class)
public class TaskDetailsView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecurityService securityService;
    private final TaskService taskService;
    private Task taskData;

    private VerticalLayout tabContent;

    public TaskDetailsView(@NotNull TaskService taskService, @NotNull SecurityService securityService) {
        this.securityService = securityService;
        this.taskService = taskService;
    }

    @Override
    public void setParameter(BeforeEvent event, String param) {
        logger.info("Task ID param: {}", param);
        try {
            long taskId = Long.parseLong(param);
            taskData = taskService.fetchByTaskIdAndUserId(taskId, securityService.getAuthenticatedUser().getId())
                    .orElseThrow(() -> new RuntimeException("No task found for given user"));
        } catch (Exception ex) {
            logger.error("Got an error during initialization task view", ex);
            event.forwardTo(TaskListView.class);
            return;
        }

        createMainContent();
    }

    private void createMainContent() {
        H2 title = new H2(taskData.getTitle());

        // Buttons block
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setWidthFull();
        btnLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button backBtn = new Button(getTranslation("go_back"), VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(event -> UI.getCurrent().navigate(TaskListView.class));

        Button editTaskDetails = new Button(getTranslation("task_details_btn_edit"), VaadinIcon.EDIT.create());
        editTaskDetails.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        editTaskDetails.addClickListener(event -> {});

        Button deleteTaskBtn = new Button(getTranslation("task_details_btn_delete"), VaadinIcon.TRASH.create());
        deleteTaskBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteTaskBtn.addClickListener(event -> {});

        // Empty label to expand it
        Label emptySpace = new Label();
        btnLayout.add(backBtn, emptySpace, editTaskDetails, deleteTaskBtn);
        btnLayout.expand(emptySpace);

        // Task other properties
        Tab description = new Tab("Description");
        Tab problems = new Tab("Problems");
        Tab publications = new Tab("Publications");

        tabContent = new VerticalLayout();

        Tabs tabs = new Tabs(description, problems, publications);
        tabs.setWidthFull();
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(description)) {
                fillDescriptionContent();
            }
        });
        tabs.setSelectedTab(description);
        fillDescriptionContent();

        add(title, btnLayout, tabs, tabContent);
    }

    private void fillDescriptionContent() {
        tabContent.removeAll();
        tabContent.add(new Html(String.format("<div>%s</div>", taskData.getDescription())));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_task_details", taskData.getTitle());
    }
}
