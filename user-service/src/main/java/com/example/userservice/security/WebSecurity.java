package com.example.userservice.security;

import com.example.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

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
        return http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                                .requestMatchers(new AntPathRequestMatcher("/health_check")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/users", HttpMethod.POST.name())).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/**"))
                                // IpAddressMatcher는 context.getRequest().getRemoteAddr()로 주소를 가져와 비교하는데
                                // localhost로 요청을 하면 IPv6 주소인 0:0:0:0:0:0:0:1을 값으로 가져옴
                                // 127.0.0.1로 요청을 하면 IPv4 주소인 127.0.0.1을 값으로 가져옴
//                                    .access(((authentication, context) -> new AuthorizationDecision(new IpAddressMatcher("0:0:0:0:0:0:0:1").matches(context.getRequest()))))
                                    .access(((authentication, context) -> new AuthorizationDecision(new IpAddressMatcher("127.0.0.1").matches(context.getRequest()))))
//                                    .access(((authentication, context) -> new AuthorizationDecision(new IpAddressMatcher("172.18.0.5").matches(context.getRequest()))))
                )
                .addFilter(getAuthenticationFilter())
                .build();

//        return http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeRequests((authz) -> authz.requestMatchers("/actuator/**").permitAll())
//                .authorizeRequests((authz) -> authz.requestMatchers("/**")
//                        .hasIpAddress("172.18.0.3")
//                        .and()
//                        .addFilter(getAuthenticationFilter()))
//                .headers(AbstractHttpConfigurer::disable)
//                .build();
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
