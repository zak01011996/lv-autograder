package lv.rtu.autograderserver.ui.component.form;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lv.rtu.autograderserver.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public class UserForm extends FormLayout {

    private User userData;
    private boolean isEditing;

    private Binder<User> binder;

    private EmailField emailField = new EmailField();
    private TextField firstNameField = new TextField();
    private TextField lastNameField = new TextField();

    private PasswordField passwordField = new PasswordField();
    private PasswordField confirmPasswordField = new PasswordField();

    private Button saveBtn = new Button(getTranslation("user_form_btn_save"));
    private Button cancelBtn = new Button(getTranslation("user_form_btn_cancel"));

    private Consumer<User> saveCallback;
    private Consumer<User> cancelCallback;

    public UserForm(@NotNull User data, boolean isEditing) {
        this.userData = data;
        this.isEditing = isEditing;

        this.binder = new Binder<>(User.class);
        binder.setBean(userData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 1));

        H3 title = new H3(getTranslation("user_form_title"));
        title.getStyle().set("margin-bottom", "1.5em");

        saveBtn.addClickListener(event -> {
            if (saveCallback != null) {
                if (binder.writeBeanIfValid(userData)) {
                    saveCallback.accept(userData);
                }
            }
        });

        cancelBtn.addClickListener(event -> {
            if (cancelCallback != null) {
                cancelCallback.accept(userData);
            }
        });

        // Set title
        add(title);

        createForm();
        createBtnLayout();

        bindData();
    }

    public void registerSaveCallback(Consumer<User> cb) {
        this.saveCallback = cb;
    }

    public void registerCancelCallback(Consumer<User> cb) {
        this.cancelCallback = cb;
    }

    private void createForm() {
        emailField.setWidthFull();
        addFormItem(emailField, getTranslation("user_form_email"));

        firstNameField.setWidthFull();
        addFormItem(firstNameField, getTranslation("user_form_first_name"));

        lastNameField.setWidthFull();
        addFormItem(lastNameField, getTranslation("user_form_last_name"));

        if (!isEditing) {
            passwordField.setWidthFull();
            addFormItem(passwordField, getTranslation("user_form_password"));

            confirmPasswordField.setWidthFull();
            addFormItem(confirmPasswordField, getTranslation("user_form_confirm_password"));
        }
    }

    private void createBtnLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickShortcut(Key.ENTER);

        cancelBtn.addClickShortcut(Key.ESCAPE);

        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("margin-top", "3em");
        layout.add(saveBtn, cancelBtn);

        add(layout);
    }

    private void bindData() {
        binder.forField(emailField).withValidator(
                new EmailValidator(getTranslation("user_form_error_email"))
        ).bind(
                User::getEmail,
                User::setEmail
        );

        binder.forField(firstNameField).withValidator(
                new StringLengthValidator(getTranslation("user_form_error_first_name"), 1, null)
        ).bind(
                User::getFirstName,
                User::setFirstName
        );

        binder.forField(lastNameField).withValidator(
                new StringLengthValidator(getTranslation("user_form_error_last_name"), 1, null)
        ).bind(
                User::getLastName,
                User::setLastName
        );


        if (!isEditing) {
            binder.forField(confirmPasswordField).withValidator((Validator<String>) (value, context) -> {
                if (passwordField.getValue().isEmpty() ||
                        !passwordField.getValue().equals(confirmPasswordField.getValue())) {
                    return ValidationResult.error(getTranslation("user_form_error_password"));
                }

                return ValidationResult.ok();
            }).bind(
                    (user) -> "",
                    (user, val) -> {
                        // Storing passwords after bcrypt
                        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                        user.setPassword(encoder.encode(val));
                    }
            );
        }
    }
}
