package com.sidetracked.api.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.User;
import com.sidetracked.api.repository.UserRepository;

/*
 * ============================================================
 * ACCOUNTS
 * ============================================================
 *
 * Creating accounts, checking passwords, and Google sign-in.
 *
 * Claiming local data: before accounts existed, everything
 * belonged to one local user with no way to sign in. When
 * sidetracked.auth.claim-local-user is true, the first account
 * created adopts that user (and all its data) instead of starting
 * empty. Once claimed, that user has a login, so nothing else can
 * claim it, even if the setting is left on. Off by default, so on
 * a server a stranger can never claim anything.
 */
@Service
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    /* BCrypt only uses the first 72 bytes of a password. */
    private static final int MAX_PASSWORD_LENGTH = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean claimLocalUser;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${sidetracked.auth.claim-local-user:false}")
            boolean claimLocalUser) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.claimLocalUser = claimLocalUser;
    }


    @Transactional
    public User register(
            String email,
            String password,
            String displayName) {

        String normalizedEmail = normalizeEmail(email);

        checkPassword(password);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with that email already exists."
            );
        }

        User user = claimableOrNewUser();

        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(cleanDisplayName(displayName, normalizedEmail));

        return userRepository.save(user);
    }


    /*
     * The user for these credentials. Wrong email and wrong
     * password give the same answer, so the response never reveals
     * which emails have accounts.
     */
    public User authenticate(String email, String password) {

        return userRepository
                .findByEmail(normalizeEmail(email))
                .filter(user -> user.getPasswordHash() != null)
                .filter(user -> passwordEncoder.matches(
                        password == null ? "" : password,
                        user.getPasswordHash()
                ))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email or password is incorrect."
                ));
    }


    /*
     * The user for a Google sign-in: an existing Google-linked user,
     * else an existing account with the same verified email (now
     * linked), else a claimed or brand-new user.
     */
    @Transactional
    public User signInWithGoogle(
            String googleSubject,
            String email,
            boolean emailVerified,
            String name) {

        return userRepository
                .findByGoogleSubject(googleSubject)
                .orElseGet(() -> {

                    String normalizedEmail =
                            email == null ? null : normalizeEmail(email);

                    User user =
                            (emailVerified && normalizedEmail != null)
                                    ? userRepository
                                            .findByEmail(normalizedEmail)
                                            .orElseGet(this::claimableOrNewUser)
                                    : claimableOrNewUser();

                    user.setGoogleSubject(googleSubject);

                    if (user.getEmail() == null
                            && emailVerified
                            && normalizedEmail != null
                            && userRepository.findByEmail(normalizedEmail).isEmpty()) {

                        user.setEmail(normalizedEmail);
                    }

                    if (user.getDisplayName() == null
                            || user.getDisplayName().equals("Local Developer")) {

                        user.setDisplayName(
                                cleanDisplayName(name, normalizedEmail)
                        );
                    }

                    return userRepository.save(user);
                });
    }


    private User claimableOrNewUser() {

        if (claimLocalUser) {

            return userRepository
                    .findFirstByEmailIsNullAndGoogleSubjectIsNullOrderByIdAsc()
                    .orElseGet(User::new);
        }

        return new User();
    }


    private String normalizeEmail(String email) {

        String normalized =
                email == null
                        ? ""
                        : email.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() > 255
                || !normalized.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter a valid email address."
            );
        }

        return normalized;
    }


    private void checkPassword(String password) {

        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords need " + MIN_PASSWORD_LENGTH + " to "
                            + MAX_PASSWORD_LENGTH + " characters."
            );
        }
    }


    private String cleanDisplayName(String displayName, String email) {

        String name =
                displayName == null ? "" : displayName.trim();

        if (name.isEmpty()) {
            name = email == null ? "Explorer" : email.split("@")[0];
        }

        return name.length() > 100 ? name.substring(0, 100) : name;
    }
}
