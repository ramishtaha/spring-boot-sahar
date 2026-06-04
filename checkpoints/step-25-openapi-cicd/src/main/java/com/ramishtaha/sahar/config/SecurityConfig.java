package com.ramishtaha.sahar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The security policy (step 24): <b>reads are public, writes require the admin</b>.
 *
 * <p>The moment Spring Security is on the classpath, EVERY request needs authentication - "secure by default".
 * This one bean then re-opens exactly what should be public and locks the rest:
 * <ul>
 *   <li>{@code GET /api/**} and the static site, PWA, and Leaflet map - <b>permitAll</b>. The home page must
 *       work for anyone with the link.</li>
 *   <li>{@code POST/PUT/DELETE /api/**} - <b>authenticated</b>. Editing the routine is yours alone.</li>
 *   <li>{@code /actuator/health} and {@code /actuator/info} stay open (probes); other actuator endpoints
 *       (metrics) require auth.</li>
 * </ul>
 *
 * <p><b>Why HTTP Basic + stateless?</b> Sahar's writer is a single person using a single page, so a session
 * and a login form would be ceremony. HTTP Basic over HTTPS, with {@code SessionCreationPolicy.STATELESS},
 * authenticates each write on its own - no {@code JSESSIONID}, nothing to fixate or leak. The admin page
 * sends the {@code Authorization: Basic} header on each write (see admin.js).
 *
 * <p><b>Why disable CSRF?</b> CSRF attacks abuse <em>ambient</em> credentials the browser attaches
 * automatically - cookies, a logged-in session. A stateless Basic-auth API has none: the browser never
 * replays Basic credentials to a cross-site {@code fetch}, so there is nothing for a forged request to ride
 * on. Disabling CSRF is the correct, documented choice for this shape of API. (Keep CSRF ON the day you move
 * to cookie/session auth.)
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // OpenAPI docs (step 25)
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().permitAll()) // index.html, CSS/JS, manifest, service worker, icons
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(frame -> frame.sameOrigin())); // the H2 console renders in a frame
        return http.build();
    }

    /** The delegating encoder stores passwords as {bcrypt}... so the hashing scheme can evolve over time. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Sahar's single admin user, materialised from {@link AdminProperties} (defaults admin/sahar). */
    @Bean
    UserDetailsService userDetailsService(AdminProperties props, PasswordEncoder encoder) {
        UserDetails admin = User.withUsername(props.username())
                .password(encoder.encode(props.password()))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
