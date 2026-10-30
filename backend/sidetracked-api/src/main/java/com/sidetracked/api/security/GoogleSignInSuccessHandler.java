package com.sidetracked.api.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sidetracked.api.model.User;
import com.sidetracked.api.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * After Google confirms who someone is: find or create their
 * SideTracked user, sign them into the session, and send them back
 * to the app.
 */
@Component
public class GoogleSignInSuccessHandler
        implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final SessionLogin sessionLogin;

    public GoogleSignInSuccessHandler(
            AuthService authService,
            SessionLogin sessionLogin) {

        this.authService = authService;
        this.sessionLogin = sessionLogin;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User google = (OAuth2User) authentication.getPrincipal();

        User user = authService.signInWithGoogle(
                google.getAttribute("sub"),
                google.getAttribute("email"),
                Boolean.TRUE.equals(google.getAttribute("email_verified")),
                google.getAttribute("name")
        );

        sessionLogin.signIn(user.getId(), request, response);

        response.sendRedirect("/");
    }
}
