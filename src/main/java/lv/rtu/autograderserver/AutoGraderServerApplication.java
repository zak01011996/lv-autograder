package lv.rtu.autograderserver;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@Theme("autograder-theme")
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class AutoGraderServerApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AutoGraderServerApplication.class, args);
    }
}
