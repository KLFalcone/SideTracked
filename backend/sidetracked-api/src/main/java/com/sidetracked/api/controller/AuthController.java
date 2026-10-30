package com.sidetracked.api.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.User;
import com.sidetracked.api.security.SessionLogin;
import com.sidetracked.api.service.AuthService;
import com.sidetracked.api.service.CurrentUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Signing up, signing in, and "who am I?".
 * Signing out is POST /api/auth/logout (see SecurityConfig).
 * Google sign-in starts at /oauth2/authorization/google.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionLogin sessionLogin;
    private final CurrentUserService currentUserService;
    private final ObjectProvider<ClientRegistrationRepository> googleClients;

    public AuthController(
            AuthService authService,
            SessionLogin sessionLogin,
            CurrentUserService currentUserService,
            ObjectProvider<ClientRegistrationRepository> googleClients) {

        this.authService = authService;
        this.sessionLogin = sessionLogin;
        this.currentUserService = currentUserService;
        this.googleClients = googleClients;
    }

    @PostMapping("/register")
    public Me register(
            @RequestBody RegisterRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = authService.register(
                body.email(),
                body.password(),
                body.displayName()
        );

        sessionLogin.signIn(user.getId(), request, response);

        return Me.of(user);
    }

    @PostMapping("/login")
    public Me login(
            @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = authService.authenticate(
                body.email(),
                body.password()
        );

        sessionLogin.signIn(user.getId(), request, response);

        return Me.of(user);
    }

    @GetMapping("/me")
    public Me me() {
        return Me.of(currentUserService.getCurrentUser());
    }

    /*
     * Which sign-in options are switched on, for the sign-in screen.
     */
    @GetMapping("/providers")
    public Providers providers() {

        return new Providers(
                googleClients.getIfAvailable() != null
        );
    }

    /*
     * Makes sure the browser has a CSRF cookie before its first
     * sign-in request. Reading the token is enough to set it.
     */
    @GetMapping("/csrf")
    public void csrf(CsrfToken token) {
        token.getToken();
    }


    public record RegisterRequest(
            String email,
            String password,
            String displayName) {
    }

    public record LoginRequest(
            String email,
            String password) {
    }

    public record Providers(boolean google) {
    }

    /*
     * What the browser may know about the signed-in user.
     */
    public record Me(
            String displayName,
            String email,
            boolean hasPassword,
            boolean hasGoogle) {

        static Me of(User user) {

            return new Me(
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getPasswordHash() != null,
                    user.getGoogleSubject() != null
            );
        }
    }
}
