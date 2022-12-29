package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.DateTimeRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.LongRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.Problem;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.SandboxType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

public class NewPublicationForm extends FormLayout {

    private Publication publicationData;
    private final Binder<Publication> binder;

    private final DateTimePicker availableFrom = new DateTimePicker();
    private final DateTimePicker availableTo = new DateTimePicker();
    private final IntegerField timeLimitField = new IntegerField();
    private final PasswordField passwordField = new PasswordField();

    private final Button saveBtn = new Button(getTranslation("publication_form_btn_save"));

    private Consumer<Publication> saveCallback;

    public NewPublicationForm() {
        this.publicationData = new Publication();

        this.binder = new Binder<>(Publication.class);
        binder.setBean(publicationData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 5));

        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(publicationData)) {
                    saveCallback.accept(publicationData);
                }
            }
        });

        createForm();
        bindData();
    }

    public void registerSaveCallback(Consumer<Publication> cb) {
        this.saveCallback = cb;
    }

    public void reset() {
        this.availableFrom.setValue(null);
        this.availableTo.setValue(null);
        this.timeLimitField.setValue(0);

        this.publicationData = new Publication();
        this.binder.setBean(publicationData);
    }

    private void createForm() {
        availableFrom.setLabel(getTranslation("publication_form_available_from"));
        availableFrom.setRequiredIndicatorVisible(true);

        availableTo.setLabel(getTranslation("publication_form_available_to"));
        availableTo.setRequiredIndicatorVisible(true);

        timeLimitField.setLabel(getTranslation("publication_form_time_limit"));

        passwordField.setLabel(getTranslation("publication_form_time_limit"));
        passwordField.setRevealButtonVisible(true);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setMaxWidth(150, Unit.PIXELS);
        saveBtn.addClickShortcut(Key.ENTER);

        add(availableFrom, availableTo, timeLimitField, passwordField, saveBtn);
    }

    private void bindData() {

        binder.forField(availableFrom).asRequired().withValidator(
                new DateTimeRangeValidator(getTranslation("publication_form_error_available_from"), LocalDateTime.now(), null)
        ).bind(
                Publication::getAvailableFrom,
                Publication::setAvailableFrom
        );


        binder.forField(availableTo).asRequired().withValidator(
                new DateTimeRangeValidator(getTranslation("publication_form_error_available_to"), LocalDateTime.now(), null)
        ).bind(
                Publication::getAvailableTo,
                Publication::setAvailableTo
        );

        binder.forField(timeLimitField).asRequired().withValidator(
                new IntegerRangeValidator(getTranslation("publication_form_error_time_limit"), 1, null)
        ).bind(
                Publication::getTimeLimit,
                Publication::setTimeLimit
        );

        binder.forField(passwordField).bind(
                Publication::getPassword,
                Publication::setPassword
        );
    }
}
