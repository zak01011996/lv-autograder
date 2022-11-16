package lv.rtu.autograderserver.ui.component;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import javax.validation.constraints.NotNull;

public class NotificationHelper {
    private static final int DEFAULT_DURATION = 3000; // 3 seconds

    public static class NotificationImpl extends Notification {
        public NotificationImpl(@NotNull String message, int duration, NotificationVariant themeVariant) {
            if (themeVariant != null) {
                addThemeVariants(themeVariant);
            }

            setPosition(Position.TOP_CENTER);
            setDuration(duration);

            Button close = new Button(new Icon("lumo", "cross"));
            close.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            close.addClickListener(event -> this.close());

            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            layout.add(new Div(new Text(message)), close);

            add(layout);
        }
    }

    public static void displayStandard(@NotNull String message) {
        displayStandard(message, DEFAULT_DURATION);
    }

    public static void displayStandard(@NotNull String message, int duration) {
        (new NotificationImpl(message, duration, null)).open();
    }


    public static void displayError(@NotNull String message) {
        displayError(message, DEFAULT_DURATION);
    }

    public static void displayError(@NotNull String message, int duration) {
        (new NotificationImpl(message, duration, NotificationVariant.LUMO_ERROR)).open();
    }


    public static void displaySuccess(@NotNull String message) {
        displaySuccess(message, DEFAULT_DURATION);
    }

    public static void displaySuccess(@NotNull String message, int duration) {
        (new NotificationImpl(message, duration, NotificationVariant.LUMO_SUCCESS)).open();
    }

}
