package msa.lime1st.cloud.authorization.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class SecurityConfig {

    private final String username;
    private final String password;

    public SecurityConfig(
        @Value("${app.eureka-username}") String username,
        @Value("${app.eureka-password}") String password
    ) {
        this.username = username;
        this.password = password;
    }

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer configurer =
            OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
            .securityMatcher(configurer.getEndpointsMatcher())
            .with(configurer, authorizationServer ->
                authorizationServer.oidc(withDefaults()))   // Enable OpenID Connect 1.0
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().authenticated())
            // Redirect to the login page when not authenticated from the
            // authorization endpoint
            .exceptionHandling(exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML))
            );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
        throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            // Form login handles the redirect to the login page from the
            // authorization server filter chain
            .formLogin(withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // NoOpPasswordEncoder 가 deprecate 되어 직접 구현
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString(); // 평문 그대로
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword); // 평문 비교
            }
        };
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails user = users
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }
}
