package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurity{
    private AuthenticationConfiguration authenticationConfiguration;
    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Environment env;

    @Autowired
    public WebSecurity(AuthenticationConfiguration authenticationConfiguration, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, Environment env) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.env = env;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeRequests((authz) -> authz.requestMatchers("/users/**").permitAll())
                .authorizeRequests((authz) -> authz.requestMatchers("/actuator/**").permitAll())
                .authorizeRequests((authz) -> authz.requestMatchers("/**")
                        .hasIpAddress("172.18.0.3")
                        .and()
                        .addFilter(getAuthenticationFilter()))
                .headers(AbstractHttpConfigurer::disable)
                .build();
    }

    private AuthenticationFilter getAuthenticationFilter() {
        try {
            AuthenticationFilter authenticationFilter = new AuthenticationFilter(
                    authenticationConfiguration.getAuthenticationManager(),
                    userService,
                    env
            );
            return authenticationFilter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
