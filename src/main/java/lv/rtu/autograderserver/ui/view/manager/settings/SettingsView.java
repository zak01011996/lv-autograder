package lv.rtu.autograderserver.ui.view.manager.settings;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lv.rtu.autograderserver.ui.view.manager.MainLayout;

import javax.annotation.security.PermitAll;

@PermitAll
@Route(value = "manager/settings", layout = MainLayout.class)
@PageTitle("Tasks management view")
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        add(new Label("Settings view will go here"));
    }
}
