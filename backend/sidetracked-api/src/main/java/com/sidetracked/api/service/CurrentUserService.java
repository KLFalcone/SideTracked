package com.sidetracked.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.User;
import com.sidetracked.api.repository.UserRepository;

/*
 * ============================================================
 * WHO IS MAKING THIS REQUEST?
 * ============================================================
 *
 * Every service asks this class for the current user and only
 * reads or changes that user's data. The user always comes from
 * the signed-in session, never from the request body or URL.
 *
 * After signing in (password or Google), the session holds an
 * authentication whose principal is the user's id; see
 * SessionLogin.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof Long userId) {

            return userId;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please sign in."
        );
    }

    public User getCurrentUser() {

        return userRepository
                .findById(getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please sign in."
                ));
    }
}
