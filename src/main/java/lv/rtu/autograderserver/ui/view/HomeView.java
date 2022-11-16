package lv.rtu.autograderserver.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route("")
@PageTitle("Code auto grader")
public class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H1("Hello, this is Home view"));
    }
}
