package com.sidetracked.api.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

/*
 * ============================================================
 * SECURITY
 * ============================================================
 *
 * - Everything under /api needs a signed-in user, except the few
 *   sign-in endpoints. Not signed in = 401, never a redirect.
 * - Sessions are cookie-based. CSRF protection uses the
 *   single-page-app setup: the server sets an XSRF-TOKEN cookie
 *   and Angular sends it back in a header on every change.
 * - "Sign in with Google" switches on only when Google OAuth keys
 *   are configured (environment variables, never committed).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> googleClients,
            GoogleSignInSuccessHandler googleSignInSuccessHandler)
            throws Exception {

        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.spa())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/providers",
                                "/api/auth/csrf"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler()
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        if (googleClients.getIfAvailable() != null) {

            http.oauth2Login(oauth -> oauth
                    .successHandler(googleSignInSuccessHandler)
                    .failureUrl("/?signin=failed")
            );
        }

        return http.build();
    }

    /*
     * BCrypt, stored with a {bcrypt} prefix so the algorithm can be
     * upgraded later without breaking existing passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
