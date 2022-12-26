package lv.rtu.autograderserver.ui.view.manager.taskmanagement;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.f0rce.ace.AceEditor;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.ProblemFile;
import lv.rtu.autograderserver.ui.component.form.FileForm;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class ProblemFileEditor extends Div {

    private final AceEditor fileEditor = new AceEditor();
    private final ListBox<ProblemFile> fileListBox = new ListBox<>();
    private final Problem problemData;
    private final Checkbox isSolutionTemplate = new Checkbox();
    private final Checkbox isContainerEntryPoint = new Checkbox();
    private ProblemFile currentFile = null;

    private VerticalLayout codeEditorLayout = new VerticalLayout();
    private VerticalLayout fileListLayout = new VerticalLayout();

    public ProblemFileEditor(@NotNull Problem problemData) {
        this.problemData = problemData;
        initForm();
        createEditorForm();
    }

    private void initForm() {
        fileEditor.addValueChangeListener(event -> currentFile.setContent(event.getValue()));

        fileListBox.setItemLabelGenerator(ProblemFile::getFileName);
        fileListBox.addValueChangeListener(event -> changeCurrentFile(event.getValue()));
        fileListBox.setItems(problemData.getFiles());

        isSolutionTemplate.setLabel(getTranslation("problem_details_file_editor_is_solution_template"));
        isSolutionTemplate.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                problemData.getFiles().forEach(f -> f.setSolutionTemplate(false));
                currentFile.setSolutionTemplate(event.getValue());
            }
        });

        isContainerEntryPoint.setLabel(getTranslation("problem_details_file_editor_is_container_entrypoint"));
        isContainerEntryPoint.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                problemData.getFiles().forEach(f -> f.setContainerEntryPoint(false));
                currentFile.setContainerEntryPoint(event.getValue());
            }
        });
    }

    private void createEditorForm() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName("bordered");
        layout.setWidthFull();

        fileListLayout = createFileListBlock();
        fileListLayout.setWidth(10, Unit.PERCENTAGE);

        codeEditorLayout = createCodeEditorBlock();
        codeEditorLayout.setWidth(90, Unit.PERCENTAGE);

        layout.add(fileListLayout, codeEditorLayout);

        add(layout);
    }

    private VerticalLayout createFileListBlock() {
        VerticalLayout layout = new VerticalLayout();

        Button newFileBtn = new Button(getTranslation("problem_details_file_editor_btn_new_file"), VaadinIcon.PLUS.create());
        newFileBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        newFileBtn.setWidthFull();
        newFileBtn.addClickListener(event -> showNewFileForm());

        fileListBox.setWidthFull();

        if (!problemData.getFiles().isEmpty()) {
            fileListBox.setValue(
                    problemData.getFiles().stream()
                            .filter(ProblemFile::isSolutionTemplate)
                            .findFirst()
                            .orElse(problemData.getFiles().get(0))
            );
        }

        layout.add(newFileBtn, new Hr(), fileListBox);
        layout.setAlignItems(FlexComponent.Alignment.END);
        return layout;
    }

    private VerticalLayout createCodeEditorBlock() {
        VerticalLayout fileListLayout = new VerticalLayout();
        fileEditor.setWidthFull();
        fileEditor.setMinHeight(600, Unit.PIXELS);

        HorizontalLayout optionsLayout = new HorizontalLayout();
        optionsLayout.setWidthFull();
        optionsLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button renameFile = new Button(getTranslation("problem_details_file_editor_btn_rename_file"),
                VaadinIcon.FILE_FONT.create());
        renameFile.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        renameFile.addClickListener(event -> showRenameForm());

        Button deleteFileBtn = new Button(getTranslation("problem_details_file_editor_btn_delete_file"),
                VaadinIcon.TRASH.create());
        deleteFileBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteFileBtn.addClickListener(event -> showDeleteFileDialog());

        // Empty label to expand it
        Label emptySpace = new Label();

        optionsLayout.add(isSolutionTemplate, isContainerEntryPoint, emptySpace, renameFile, deleteFileBtn);
        optionsLayout.expand(emptySpace);

        Label info = new Label(getTranslation("problem_details_file_editor_info", problemData.getSandboxType()));
        fileListLayout.add(info, new Hr(), optionsLayout, fileEditor);
        return fileListLayout;
    }

    private void showNewFileForm() {
        ProblemFile problemFile = new ProblemFile(problemData);
        showFileForm(problemFile, true);
    }

    private void showRenameForm() {
        showFileForm(currentFile, false);
    }

    private void showFileForm(@NotNull ProblemFile problemFile, boolean isNew) {
        Dialog dialog = new Dialog();
        dialog.setWidth(45, Unit.EM);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        FileForm form = new FileForm(problemFile);
        form.registerSaveCallback(data -> {
            if (isNew) {
                problemData.getFiles().add(data);
            }

            fileListBox.setItems(problemData.getFiles());
            fileListBox.setValue(data);
            dialog.close();
        });

        form.registerCancelCallback(data -> dialog.close());

        dialog.add(form);
        dialog.open();
    }

    @SuppressWarnings("Duplicates")
    private void showDeleteFileDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCancelable(true);
        dialog.setHeader(getTranslation("confirm_dialog_title"));
        dialog.setText(getTranslation("confirm_dialog_text_delete_file"));
        dialog.setConfirmText(getTranslation("confirm_dialog_btn_delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText(getTranslation("confirm_dialog_btn_cancel"));
        dialog.addConfirmListener(event1 -> {
            currentFile.setProblem(null);
            problemData.getFiles().removeIf(f -> f.equals(currentFile));
            fileListBox.setItems(problemData.getFiles());

            if (!problemData.getFiles().isEmpty()) {
                fileListBox.setValue(problemData.getFiles().get(0));
            } else {
                fileListBox.setValue(null);
            }
        });

        dialog.open();
    }

    private void changeCurrentFile(@Nullable ProblemFile file) {
        if (file == null) {
            currentFile = null;
            codeEditorLayout.setVisible(false);
            return;
        }

        codeEditorLayout.setVisible(true);

        currentFile = file;
        fileEditor.setValue(Optional.ofNullable(file.getContent()).orElse(""));
        isSolutionTemplate.setReadOnly(false);
        isSolutionTemplate.setValue(false);

        isContainerEntryPoint.setReadOnly(false);
        isContainerEntryPoint.setValue(false);

        if (currentFile.isSolutionTemplate()) {
            isSolutionTemplate.setReadOnly(true);
            isSolutionTemplate.setValue(true);
        }

        if (currentFile.isContainerEntryPoint()) {
            isContainerEntryPoint.setReadOnly(true);
            isContainerEntryPoint.setValue(true);
        }
    }
}
