package lv.rtu.autograderserver.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lv.rtu.autograderserver.ui.view.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/images/**").permitAll();

        // Open API, so it controls authentication on it side
        http.authorizeRequests().antMatchers("/api/**").permitAll();

        // Solution page should not go under security
        http.authorizeRequests().antMatchers("/solve/**").permitAll();

        // Solution page should not go under security
        http.authorizeRequests().antMatchers("/ace-builds/**").permitAll();

        super.configure(http);

        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
