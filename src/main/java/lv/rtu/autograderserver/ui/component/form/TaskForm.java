package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.Task;
import org.vaadin.tinymce.TinyMce;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class TaskForm extends FormLayout {

    private final Task taskData;
    private final Binder<Task> binder;

    private final TextField titleField = new TextField();
    private final TinyMce descriptionField = new TinyMce();

    private final Button saveBtn = new Button(getTranslation("task_form_btn_save"));
    private final Button cancelBtn = new Button(getTranslation("task_form_btn_cancel"));

    private Consumer<Task> saveCallback;
    private Consumer<Task> cancelCallback;

    public TaskForm(@NotNull Task taskData) {
        this(taskData, true, true);
    }

    public TaskForm(@NotNull Task taskData, boolean showTitle, boolean showCancelBtn) {
        this.taskData = taskData;

        this.binder = new Binder<>(Task.class);
        binder.setBean(taskData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 1));


        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(taskData)) {
                    saveCallback.accept(taskData);
                }
            }
        });

        cancelBtn.addClickListener(event -> {
            if (cancelCallback != null) {
                cancelCallback.accept(taskData);
            }
        });

        // Set title
        if (showTitle) {
            H3 title = new H3(getTranslation("task_form_title"));
            title.getStyle().set("margin-bottom", "1.5em");
            add(title);
        }

        createForm();
        createBtnLayout(showCancelBtn);

        bindData();
    }

    public void registerSaveCallback(Consumer<Task> cb) {
        this.saveCallback = cb;
    }

    public void registerCancelCallback(Consumer<Task> cb) {
        this.cancelCallback = cb;
    }

    private void createForm() {
        titleField.setWidthFull();
        addFormItem(titleField, getTranslation("task_form_name"));

        // Disable menu bar
        descriptionField.configure("menubar", false);
        descriptionField.configure("placeholder", getTranslation("task_form_description_placeholder"));
        descriptionField.setWidthFull();
        descriptionField.setMinHeight(15, Unit.EM);
        descriptionField.setHeight(25, Unit.EM);
        addFormItem(descriptionField, getTranslation("task_form_description"));
    }

    @SuppressWarnings("Duplicates")
    private void createBtnLayout(boolean showCancelBtn) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("margin-top", "1.5em");
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);
        layout.add(saveBtn);

        if (showCancelBtn) {
            cancelBtn.addClickShortcut(Key.ESCAPE);
            layout.add(cancelBtn);
        }

        add(layout);
    }

    private void bindData() {
        binder.forField(titleField).withValidator(
                new StringLengthValidator(getTranslation("task_form_error_name"), 1, null)
        ).bind(
                Task::getTitle,
                Task::setTitle
        );

        binder.forField(descriptionField).bind(
                Task::getDescription,
                Task::setDescription
        );
    }
}
