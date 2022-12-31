package lv.rtu.autograderserver.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lv.rtu.autograderserver.ui.component.LanguageSwitcher;

@AnonymousAllowed
@Route("")
public class HomeView extends VerticalLayout implements HasDynamicTitle {

    public HomeView() {
        setWidthFull();
        H1 title = new H1(getTranslation("home_view_welcome_title"));

        VerticalLayout banner = new VerticalLayout();
        banner.addClassNames("bordered");
        banner.setWidth(800, Unit.PIXELS);

        Label bannerMsg = new Label(getTranslation("home_view_welcome_message"));
        bannerMsg.setWidthFull();
        bannerMsg.getStyle().set("text-align", "center");
        FormLayout form = createFormLayout();

        banner.add(bannerMsg, form);

        LanguageSwitcher languageSwitcher = new LanguageSwitcher(true);

        add(title, banner, languageSwitcher);

        setHorizontalComponentAlignment(Alignment.CENTER, title, banner, languageSwitcher);
    }

    private FormLayout createFormLayout() {
        TextField publicationIdField = new TextField();
        publicationIdField.setPlaceholder(getTranslation("home_view_publication_field_placeholder"));

        Button goBtn = new Button(getTranslation("home_view_btn_go"));
        goBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goBtn.addClickListener(event -> {
            if (publicationIdField.isEmpty()) {
                publicationIdField.getElement().setAttribute("invalid", "");
                return;
            }

            UI.getCurrent().navigate(AssignmentView.class,
                    new RouteParameters("publicationId", publicationIdField.getValue()));
        });

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 5));
        formLayout.add(publicationIdField, goBtn);
        formLayout.setColspan(publicationIdField, 4);

        return formLayout;
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_home");
    }
}
