package accordion_symphonic.ticketing.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService adminUsers(
            @Value("${ticketing.security.admin.username}") String username,
            @Value("${ticketing.security.admin.password}") String password,
            PasswordEncoder passwordEncoder
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Statische Dateien
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/admin.html",
                                "/app.js",
                                "/admin.js",
                                "/style.css",
                                "/favicon.ico"
                        ).permitAll()

                        // Öffentliche Konzertübersicht
                        .requestMatchers(HttpMethod.GET,
                                "/api/concerts",
                                "/api/concerts/{concertId}/ticket-categories")
                        .permitAll()

                        // Kunden dürfen eine Bestellung erstellen
                        .requestMatchers(HttpMethod.POST,
                                "/api/concerts/{concertId}/orders")
                        .permitAll()

                        // Kunden dürfen ihre Bestellung mit Access-Token abrufen
                        .requestMatchers(HttpMethod.GET,
                                "/api/concerts/{concertId}/orders/{orderId}")
                        .permitAll()

                        // Kunden dürfen ihre Bestellung mit Access-Token stornieren
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/concerts/{concertId}/orders/{orderId}/cancel")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/webhooks/payments")
                        .permitAll()

                        // Admin-Bereich
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/error")
                        .permitAll()

                        // Alles andere bleibt gesperrt
                        .anyRequest()
                        .denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .build();
    }
}