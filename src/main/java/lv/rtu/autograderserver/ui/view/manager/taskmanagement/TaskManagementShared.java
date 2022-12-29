package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import lv.rtu.autograderserver.model.AuditMetadata;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.model.User;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.service.TaskService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.component.form.TaskForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Component
/**
 * For shared UI components related to tasks management
 */
public class TaskManagementShared {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskService taskService;
    private final SecurityService securityService;

    public TaskManagementShared(@NotNull TaskService taskService, @NotNull SecurityService securityService) {
        this.taskService = taskService;
        this.securityService = securityService;
    }

    public void showTaskForm(@NotNull Task task, @Nullable User user, @Nullable Consumer<Task> cb) {
        Dialog dialog = new Dialog();
        dialog.setWidth(45, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TaskForm form = new TaskForm(task);
        form.registerSaveCallback(data -> {
            try {
                // Tasks should be created under selected user, not under superuser
                if (securityService.isAdmin() && user != null) {
                    if (data.getAudit() == null) {
                        data.setAudit(new AuditMetadata());
                    }

                    data.getAudit().setCreatedBy(user.getId());
                }

                Task res = taskService.saveTask(data);
                NotificationHelper.displaySuccess(getTranslation("task_form_message_success"));
                if (cb != null) {
                    cb.accept(res);
                }

                dialog.close();
            } catch (Exception exception) {
                NotificationHelper.displayError(getTranslation("task_form_message_error"));
                logger.error("Cannot save task: ", exception);
            }
        });

        form.registerCancelCallback(data -> dialog.close());

        dialog.add(form);
        dialog.open();
    }

    public void showDeleteTaskDialog(@NotNull Task taskData, @Nullable Callable<Void> cb) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setHeader(getTranslation("confirm_dialog_title"));
        dialog.setText(getTranslation("confirm_dialog_text_delete_task"));
        dialog.setConfirmText(getTranslation("confirm_dialog_btn_delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText(getTranslation("confirm_dialog_btn_cancel"));
        dialog.addConfirmListener(event1 -> {
            try {
                taskService.deleteTask(taskData);
                NotificationHelper.displaySuccess(getTranslation("task_details_message_deleted"));

                if (cb != null) {
                    cb.call();
                }

            } catch (Exception ex) {
                logger.error("Cannot delete task", ex);
                NotificationHelper.displayError(getTranslation("task_details_message_deleted_error"));
            }
        });

        dialog.open();
    }

    @SuppressWarnings("Duplicates")
    public void showDeletePublicationDialog(@NotNull Task taskData, @NotNull Publication publication, @Nullable Callable<Void> cb) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setHeader(getTranslation("confirm_dialog_title"));
        dialog.setText(getTranslation("confirm_dialog_text_delete_publication"));
        dialog.setConfirmText(getTranslation("confirm_dialog_btn_delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText(getTranslation("confirm_dialog_btn_cancel"));
        dialog.addConfirmListener(event1 -> {
            try {
                taskService.deletePublication(taskData, publication);
                NotificationHelper.displaySuccess(getTranslation("task_details_message_publication_deleted"));

                if (cb != null) {
                    cb.call();
                }
            } catch (Exception ex) {
                logger.error("Cannot delete task", ex);
                NotificationHelper.displayError(getTranslation("task_details_message_deleted_error"));
            }
        });

        dialog.open();
    }

    private String getTranslation(@NotNull String name, Object... params) {
        return UI.getCurrent().getTranslation(name, params);
    }
}
