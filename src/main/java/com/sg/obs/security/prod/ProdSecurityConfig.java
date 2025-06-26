package com.sg.obs.security.prod;


import com.sg.obs.security.repository.UserRepository;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Import({VaadinAwareSecurityContextHolderStrategyConfiguration.class})
@Profile({"prod", "production"})
public class ProdSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(ProdSecurityConfig.class);

    private final UserRepository userRepository;

    ProdSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.warn("Using PRODUCTION security configuration");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(ProdLoginView.LOGIN_PATH))
                .build();
    }


    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    VaadinServiceInitListener prodLoginConfigurer() {
        return serviceInitEvent -> {
            var routeConfiguration = RouteConfiguration.forApplicationScope();
            routeConfiguration.setRoute(ProdLoginView.LOGIN_PATH, ProdLoginView.class);
        };
    }
}
