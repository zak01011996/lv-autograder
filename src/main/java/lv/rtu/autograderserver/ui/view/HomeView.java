package lv.rtu.autograderserver.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route("")
public class HomeView extends VerticalLayout implements HasDynamicTitle {
    public HomeView() {
        add(new H1("Hello, this is Home view"));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_home");
    }
}
