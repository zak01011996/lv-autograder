package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.SandboxType;

import java.util.Optional;
import java.util.function.Consumer;

public class NewProblemForm extends FormLayout {

    private Problem problemData;
    private final Binder<Problem> binder;

    private final TextField titleField = new TextField();
    private final Select<SandboxType> sandboxTypeField = new Select<>();
    private final Button saveBtn = new Button(getTranslation("problem_form_btn_save"));

    private Consumer<Problem> saveCallback;

    public NewProblemForm() {
        this.problemData = new Problem();

        this.binder = new Binder<>(Problem.class);
        binder.setBean(problemData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 3));

        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(problemData)) {
                    saveCallback.accept(problemData);
                }
            }
        });

        createForm();
        bindData();
    }

    public void registerSaveCallback(Consumer<Problem> cb) {
        this.saveCallback = cb;
    }

    public void reset() {
        this.titleField.setValue("");
        this.sandboxTypeField.setValue(SandboxType.JAVA11);

        this.problemData = new Problem();
        this.binder.setBean(problemData);
    }

    private void createForm() {
        titleField.setWidthFull();
        titleField.setLabel(getTranslation("problem_form_name"));
        titleField.setPlaceholder(getTranslation("problem_form_name_placeholder"));

        sandboxTypeField.setLabel(getTranslation("problem_form_sandbox_type"));
        sandboxTypeField.setItems(SandboxType.values());
        sandboxTypeField.setEmptySelectionAllowed(false);
        sandboxTypeField.setValue(SandboxType.JAVA11);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setMaxWidth(150, Unit.PIXELS);
        saveBtn.addClickShortcut(Key.ENTER);

        add(titleField, sandboxTypeField, saveBtn);
    }

    private void bindData() {
        binder.forField(titleField).withValidator(
                new StringLengthValidator(getTranslation("problem_form_error_name"), 1, null)
        ).bind(
                Problem::getTitle,
                Problem::setTitle
        );

        binder.forField(sandboxTypeField).bind(
                problemData -> Optional.ofNullable(problemData.getSandboxType()).orElse(SandboxType.JAVA11),
                Problem::setSandboxType
        );
    }
}
