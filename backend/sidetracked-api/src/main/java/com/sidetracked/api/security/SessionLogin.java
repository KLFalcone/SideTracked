package com.sidetracked.api.security;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Signs a user into the current browser session, the same way for
 * password and Google sign-in. The session then holds an
 * authentication whose principal is the user's id, which
 * CurrentUserService reads on every request.
 */
@Component
public class SessionLogin {

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public void signIn(
            Long userId,
            HttpServletRequest request,
            HttpServletResponse response) {

        /*
         * A new session id on sign-in, so an id someone else may
         * have planted in the browser is never trusted.
         */
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }
}
