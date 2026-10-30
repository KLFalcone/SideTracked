package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.User;
import com.sidetracked.api.repository.UserRepository;

/*
 * Account rules, tested without a database. The fake repository
 * keeps users in a list.
 */
class AuthServiceTest {

    private final PasswordEncoder passwordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final List<User> users = new ArrayList<>();
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);

        when(userRepository.save(any(User.class))).thenAnswer(call -> {

            User user = call.getArgument(0);

            if (user.getId() == null) {
                ReflectionTestUtils.setField(user, "id", (long) users.size() + 1);
                users.add(user);
            }

            return user;
        });

        when(userRepository.findByEmail(any())).thenAnswer(call ->
                users.stream()
                        .filter(u -> call.getArgument(0).equals(u.getEmail()))
                        .findFirst());

        when(userRepository.findByGoogleSubject(any())).thenAnswer(call ->
                users.stream()
                        .filter(u -> call.getArgument(0).equals(u.getGoogleSubject()))
                        .findFirst());

        when(userRepository
                .findFirstByEmailIsNullAndGoogleSubjectIsNullOrderByIdAsc())
                .thenAnswer(call -> users.stream()
                        .filter(User::hasNoLogin)
                        .findFirst());
    }

    private AuthService service(boolean claimLocalUser) {
        return new AuthService(userRepository, passwordEncoder, claimLocalUser);
    }

    @Test
    void registeringHashesThePasswordAndNormalizesTheEmail() {

        User user = service(false).register(
                "  Kat@Example.COM ", "correct horse", "Kat");

        assertThat(user.getEmail()).isEqualTo("kat@example.com");
        assertThat(user.getPasswordHash())
                .isNotEqualTo("correct horse")
                .startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("correct horse", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    void anEmailCanOnlyBeUsedOnce() {

        service(false).register("kat@example.com", "correct horse", "Kat");

        assertThatThrownBy(() -> service(false)
                .register("KAT@example.com", "another one!", "Kat"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void weakPasswordsAndBadEmailsAreRejected() {

        assertThatThrownBy(() -> service(false)
                .register("kat@example.com", "short", "Kat"))
                .hasMessageContaining("400");

        assertThatThrownBy(() -> service(false)
                .register("not-an-email", "correct horse", "Kat"))
                .hasMessageContaining("400");
    }

    @Test
    void signingInChecksThePasswordWithoutRevealingWhichEmailsExist() {

        service(false).register("kat@example.com", "correct horse", "Kat");

        assertThat(service(false)
                .authenticate("KAT@example.com", "correct horse")
                .getEmail())
                .isEqualTo("kat@example.com");

        String wrongPassword = messageOf(() -> service(false)
                .authenticate("kat@example.com", "wrong password"));

        String unknownEmail = messageOf(() -> service(false)
                .authenticate("nobody@example.com", "correct horse"));

        assertThat(wrongPassword)
                .contains("401")
                .isEqualTo(unknownEmail);
    }

    @Test
    void withClaimingOnTheFirstAccountAdoptsTheLocalUser() {

        User local = existingLocalUser();

        User account = service(true)
                .register("kat@example.com", "correct horse", "Kat");

        assertThat(account).isSameAs(local);
        assertThat(account.getDisplayName()).isEqualTo("Kat");

        User second = service(true)
                .register("friend@example.com", "correct horse", "Friend");

        assertThat(second.getId()).isNotEqualTo(local.getId());
    }

    @Test
    void withClaimingOffAnAccountNeverTakesTheLocalUser() {

        User local = existingLocalUser();

        User account = service(false)
                .register("stranger@example.com", "correct horse", "Stranger");

        assertThat(account.getId()).isNotEqualTo(local.getId());
        assertThat(local.hasNoLogin()).isTrue();
    }

    @Test
    void googleLinksToTheAccountWithTheSameVerifiedEmail() {

        User account = service(false)
                .register("kat@example.com", "correct horse", "Kat");

        User viaGoogle = service(false).signInWithGoogle(
                "google-123", "Kat@example.com", true, "Kat F");

        assertThat(viaGoogle).isSameAs(account);
        assertThat(account.getGoogleSubject()).isEqualTo("google-123");

        assertThat(service(false).signInWithGoogle(
                "google-123", "kat@example.com", true, "Kat F"))
                .isSameAs(account);
    }

    @Test
    void googleNeverLinksOnAnUnverifiedEmail() {

        User account = service(false)
                .register("kat@example.com", "correct horse", "Kat");

        User viaGoogle = service(false).signInWithGoogle(
                "google-999", "kat@example.com", false, "Someone");

        assertThat(viaGoogle.getId()).isNotEqualTo(account.getId());
        assertThat(viaGoogle.getEmail()).isNull();
    }


    private User existingLocalUser() {

        User local = new User("Local Developer");

        userRepository.save(local);

        return local;
    }

    private String messageOf(Runnable action) {

        try {
            action.run();
            return "no error";
        } catch (ResponseStatusException e) {
            return e.getMessage();
        }
    }
}
