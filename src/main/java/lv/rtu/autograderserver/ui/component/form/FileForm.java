package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.ProblemFile;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public class FileForm extends FormLayout {

    private ProblemFile problemFile;
    private final Binder<ProblemFile> binder;

    private final TextField fileName = new TextField();
    private final Button saveBtn = new Button(getTranslation("file_form_btn_save"));
    private final Button cancelBtn = new Button(getTranslation("file_form_btn_cancel"));

    private Consumer<ProblemFile> saveCallback;
    private Consumer<ProblemFile> cancelCallback;

    public FileForm(@NotNull ProblemFile file) {
        this.problemFile = file;

        this.binder = new Binder<>(ProblemFile.class);
        binder.setBean(problemFile);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 1));

        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(problemFile)) {
                    saveCallback.accept(problemFile);
                }
            }
        });

        cancelBtn.addClickListener(event -> {
            if (cancelCallback != null) {
                cancelCallback.accept(problemFile);
            }
        });

        createForm();
        createBtnLayout();

        bindData();
    }

    public void registerSaveCallback(Consumer<ProblemFile> cb) {
        this.saveCallback = cb;
    }

    public void registerCancelCallback(Consumer<ProblemFile> cb) {
        this.cancelCallback = cb;
    }

    private void createForm() {
        fileName.setWidthFull();
        fileName.setLabel(getTranslation("file_form_file_name_label"));
        fileName.setPlaceholder(getTranslation("file_form_file_name_placeholder"));

        add(fileName);
    }

    @SuppressWarnings("Duplicates")
    private void createBtnLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        cancelBtn.addClickShortcut(Key.ESCAPE);

        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("margin-top", "1.5em");
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layout.add(saveBtn, cancelBtn);

        add(layout);
    }

    private void bindData() {
        binder.forField(fileName).withValidator(
                new StringLengthValidator(getTranslation("file_form_file_name_error"), 1, null)
        ).bind(
                ProblemFile::getFileName,
                ProblemFile::setFileName
        );
    }
}
