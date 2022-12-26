package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.ProblemFile;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;

@PermitAll
@Route(value = "manager/task/:taskId/problem/:problemId", layout = MainLayout.class)
public class ProblemConfigurationView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecurityService securityService;
    private final TaskService taskService;

    private Task taskData;
    private Problem problemData;

    private final TextField titleField = new TextField();
    private final TinyMce descriptionField = new TinyMce();

    public ProblemConfigurationView(@NotNull TaskService taskService, @NotNull SecurityService securityService) {
        this.securityService = securityService;
        this.taskService = taskService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            long taskId = Long.parseLong(event.getRouteParameters().get("taskId").orElseThrow(() ->
                    new RuntimeException("TaskID param not provided")));

            long problemId = Long.parseLong(event.getRouteParameters().get("problemId").orElseThrow(() ->
                    new RuntimeException("ProblemID param not provided")));

            taskData = taskService.fetchByTaskIdAndUserId(taskId, securityService.getAuthenticatedUser().getId())
                    .orElseThrow(() -> new RuntimeException("No task found for given user"));

            problemData = taskData.getProblems().stream().filter(p -> p.getId() == problemId).findFirst().orElseThrow();
        } catch (Exception ex) {
            logger.error("Got an error during initialization task view", ex);
            event.forwardTo(TaskListView.class);
            return;
        }

        createMainContent();
    }

    private void createMainContent() {
        H2 title = new H2(getTranslation("problem_details_title", problemData.getTitle()));

        // Buttons block
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setWidthFull();
        btnLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        Button backBtn = new Button(getTranslation("go_back"), VaadinIcon.ARROW_BACKWARD.create());
        backBtn.addClickListener(event -> UI.getCurrent().navigate(TaskDetailsView.class,
                new RouteParameters("taskId", String.valueOf(taskData.getId()))));

        Button saveBtn = new Button(getTranslation("problem_details_btn_save"), VaadinIcon.FILE_O.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickListener(event -> saveProblem());

        Button deleteBtn = new Button(getTranslation("problem_details_btn_delete"), VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteBtn.addClickListener(event -> deleteProblem());

        // Empty label to expand it
        Label emptySpace = new Label();
        btnLayout.add(backBtn, emptySpace, saveBtn, deleteBtn);
        btnLayout.expand(emptySpace);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        titleField.setWidthFull();
        titleField.setPlaceholder(getTranslation("problem_details_title_placeholder"));
        titleField.setValue(problemData.getTitle());
        titleField.addValueChangeListener(event -> problemData.setTitle(event.getValue()));

        descriptionField.setWidthFull();
        descriptionField.configure("menubar", false);
        descriptionField.setValue(problemData.getDescription());
        descriptionField.addValueChangeListener(event -> problemData.setDescription(event.getValue()));

        form.addFormItem(titleField, getTranslation("problem_details_title_label"));
        form.addFormItem(descriptionField, getTranslation("problem_details_description_label"));

        form.add(new Hr());
        form.addFormItem(createFileEditorLayout(), getTranslation("problem_details_files_label"));

        add(title, btnLayout, form);
    }

    private Component createFileEditorLayout() {
//        problemData.getFiles().addAll(List.of(
//                new ProblemFile("test.txt", "blah, blah, blah aaopisdjk0aosjdpaos", problemData),
//                new ProblemFile("test2.txt", "Some other content...", problemData),
//                new ProblemFile("run.sh", "cd /app && ls -ahl", false, true, problemData),
//                new ProblemFile("Solution.java", "package com.solution;\n\npublic class Solution {\n\n    public static int solution() {\n        //TODO: Student solution goes here...\n    }\n}", true, problemData)
//        ));
        return new ProblemFileEditor(problemData);
    }

    private void saveProblem() {
        try {
            taskService.saveTask(taskData);
            NotificationHelper.displaySuccess(getTranslation("problem_details_msg_save_changes_success"));
        } catch (Exception ex) {
            logger.error("Cannot save task data", ex);
            NotificationHelper.displayError(getTranslation("problem_details_msg_save_changes_error"));
        }
    }

    private void deleteProblem() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setHeader(getTranslation("confirm_dialog_title"));
        dialog.setText(getTranslation("confirm_dialog_text_delete_problem"));
        dialog.setConfirmText(getTranslation("confirm_dialog_btn_delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText(getTranslation("confirm_dialog_btn_cancel"));
        dialog.addConfirmListener(event1 -> {
            try {
                taskService.deleteProblem(taskData, problemData);
                dialog.close();
                UI.getCurrent().navigate(TaskDetailsView.class,
                        new RouteParameters("taskId", String.valueOf(taskData.getId())));
            } catch (Exception ex) {
                logger.error("Cannot delete problem", ex);
                NotificationHelper.displayError(getTranslation("problem_details_msg_delete_error"));
            }
        });

        dialog.open();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_problem_details", problemData.getTitle());
    }
}
