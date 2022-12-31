package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.Participant;
import lv.rtu.autograderserver.model.Task;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class ParticipantRegistrationForm extends FormLayout {

    private final Participant participantData;
    private final Binder<Participant> binder;

    private final TextField identifierField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();

    private final Button saveBtn = new Button(getTranslation("participant_reg_form_btn_save"));

    private Consumer<Participant> saveCallback;

    public ParticipantRegistrationForm(@NotNull Participant participantData) {
        this.participantData = participantData;

        this.binder = new Binder<>(Participant.class);
        binder.setBean(participantData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 1));

        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(participantData)) {
                    saveCallback.accept(participantData);
                }
            }
        });

        // Set title
        H3 title = new H3(getTranslation("participant_reg_form_title"));
        title.getStyle().set("margin-bottom", "1.5em");
        add(title);

        createForm();
        createBtnLayout();

        bindData();
    }

    public void registerSaveCallback(Consumer<Participant> cb) {
        this.saveCallback = cb;
    }

    private void createForm() {
        identifierField.setWidthFull();
        identifierField.setRequired(true);
        identifierField.setPlaceholder(getTranslation("participant_reg_form_identifier_placeholder"));
        addFormItem(identifierField, getTranslation("participant_reg_form_identifier"));

        firstNameField.setWidthFull();
        firstNameField.setRequired(true);
        firstNameField.setPlaceholder(getTranslation("participant_reg_form_first_name_placeholder"));
        addFormItem(firstNameField, getTranslation("participant_reg_form_first_name"));

        lastNameField.setWidthFull();
        lastNameField.setRequired(true);
        lastNameField.setPlaceholder(getTranslation("participant_reg_form_last_name_placeholder"));
        addFormItem(lastNameField, getTranslation("participant_reg_form_last_name"));
    }

    @SuppressWarnings("Duplicates")
    private void createBtnLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("margin-top", "1.5em");
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);
        layout.add(saveBtn);

        add(layout);
    }

    private void bindData() {
        binder.forField(identifierField).withValidator(
                new StringLengthValidator(getTranslation("participant_reg_form_identifier_error"), 1, null)
        ).bind(
                Participant::getIdentifier,
                Participant::setIdentifier
        );

        binder.forField(firstNameField).withValidator(
                new StringLengthValidator(getTranslation("participant_reg_form_first_name_error"), 1, null)
        ).bind(
                Participant::getFirstName,
                Participant::setFirstName
        );

        binder.forField(lastNameField).withValidator(
                new StringLengthValidator(getTranslation("participant_reg_form_last_name_error"), 1, null)
        ).bind(
                Participant::getLastName,
                Participant::setLastName
        );
    }
}
