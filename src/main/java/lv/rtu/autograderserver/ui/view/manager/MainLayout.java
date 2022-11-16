package lv.rtu.autograderserver.ui.view.manager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import lv.rtu.autograderserver.security.LoggedInUser;
import lv.rtu.autograderserver.security.SecurityService;
import lv.rtu.autograderserver.ui.component.LanguageSwitcher;
import lv.rtu.autograderserver.ui.view.manager.settings.SettingsView;
import lv.rtu.autograderserver.ui.view.manager.taskmanagement.TaskListView;
import lv.rtu.autograderserver.ui.view.manager.usermanagement.UsersManagementView;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class MainLayout extends AppLayout {
    private SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 appTitle = new H1(getTranslation("app_title"));
        appTitle.addClassNames("text-l", "m-m");

        LoggedInUser loggedInUser = securityService.getAuthenticatedUser();

        Span welcomeText = new Span(
                getTranslation("app_welcome_text", loggedInUser.getFirstName(), loggedInUser.getLastName())
        );
        LanguageSwitcher languageSwitcher = new LanguageSwitcher(false);

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                appTitle,
                welcomeText,
                languageSwitcher
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(appTitle);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    // Left menu navigation
    private void createDrawer() {
        Tabs menu = new Tabs();
        menu.setOrientation(Tabs.Orientation.VERTICAL);
        menu.setWidthFull();
        menu.addClassNames("left_menu");

        Tab taskListView = createMenuNavigationItem(
                getTranslation("left_menu_tasks"), VaadinIcon.TASKS, TaskListView.class);
        menu.add(taskListView);

        Tab settingsView = createMenuNavigationItem(
                getTranslation("left_menu_settings"), VaadinIcon.COG_O, SettingsView.class);
        menu.add(settingsView);

        Tab usersManagementView = createMenuNavigationItem(
                getTranslation("left_menu_users"), VaadinIcon.USER_CARD, UsersManagementView.class);
        if (securityService.getAuthenticatedUser().isAdmin()) {
            menu.add(usersManagementView);
        }

        Tab logout = new Tab(VaadinIcon.EXIT.create(), new Span(getTranslation("left_menu_logout")));
        logout.getStyle().set("cursor", "pointer");
        menu.add(logout);


        // Set active tab by current route
        UI.getCurrent().getPage().fetchCurrentURL(url -> {
            RouteConfiguration.forSessionScope().getRoute(url.getPath()).ifPresent(val -> {
                if (val.equals(SettingsView.class)) {
                    menu.setSelectedTab(settingsView);
                } else if (val.equals(UsersManagementView.class)) {
                    menu.setSelectedTab(usersManagementView);
                } else {
                    menu.setSelectedTab(taskListView);
                }
            });
        });

        menu.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(logout)) {
                securityService.logout();
            }
        });

        addToDrawer(menu);
    }

    private Tab createMenuNavigationItem(@NotNull String title, @Nullable VaadinIcon icon, @NotNull Class<? extends Component> target) {
        Tab res = new Tab();
        RouterLink link = new RouterLink(target);
        if (icon != null) {
            res.add(icon.create());
        }

        link.add(new Span(title));
        res.add(link);
        res.getStyle().set("cursor", "pointer");

        return res;
    }
}
