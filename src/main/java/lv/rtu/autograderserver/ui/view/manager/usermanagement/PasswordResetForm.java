package lv.rtu.autograderserver.ui.view.manager.usermanagement;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import lv.rtu.autograderserver.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public class PasswordResetForm extends FormLayout {

    private User userData;
    private Binder<User> binder;

    private PasswordField passwordField = new PasswordField();
    private PasswordField confirmPasswordField = new PasswordField();

    private Button saveBtn = new Button(getTranslation("user_management_form_btn_save"));
    private Button cancelBtn = new Button(getTranslation("user_management_form_btn_cancel"));

    private Consumer<User> saveCallback;
    private Consumer<User> cancelCallback;

    public PasswordResetForm(@NotNull User data) {
        this.userData = data;

        this.binder = new Binder<>(User.class);
        binder.setBean(userData);

        setWidthFull();
        setResponsiveSteps(new ResponsiveStep("0", 1));

        H3 title = new H3(getTranslation("user_management_password_reset_title"));
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
        passwordField.setWidthFull();
        addFormItem(passwordField, getTranslation("user_management_form_password"));

        confirmPasswordField.setWidthFull();
        addFormItem(confirmPasswordField, getTranslation("user_management_form_confirm_password"));
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
        binder.forField(confirmPasswordField).withValidator((Validator<String>) (value, context) -> {
            if (passwordField.getValue().isEmpty() ||
                    !passwordField.getValue().equals(confirmPasswordField.getValue())) {
                return ValidationResult.error(getTranslation("user_management_form_error_password"));
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
