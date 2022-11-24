package lv.rtu.autograderserver.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import lv.rtu.autograderserver.ui.component.LanguageSwitcher;

/**
 * Author notes:
 * Code was taken and modified from official documentation
 * Source: https://vaadin.com/docs/latest/tutorial/login-and-authentication
 */
@Route("login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setI18n(getLoginI18N());
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        LanguageSwitcher languageSwitcher = new LanguageSwitcher(true);

        add(new H1(getTranslation("app_title")), loginForm, languageSwitcher);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {

            loginForm.setError(true);
        }
    }

    private LoginI18n getLoginI18N() {
        LoginI18n res = LoginI18n.createDefault();

        LoginI18n.Form form = res.getForm();

        form.setTitle(getTranslation("login_form_title"));
        form.setUsername(getTranslation("login_form_username"));
        form.setPassword(getTranslation("login_form_password"));
        form.setSubmit(getTranslation("login_form_submit_button"));
        res.setForm(form);

        LoginI18n.ErrorMessage i18nErrorMessage = res.getErrorMessage();
        i18nErrorMessage.setTitle(getTranslation("login_form_error_title"));
        i18nErrorMessage.setMessage(getTranslation("login_form_error_message"));
        res.setErrorMessage(i18nErrorMessage);

        return res;
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_login");
    }
}
