package com.sidetracked.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.User;
import com.sidetracked.api.repository.UserRepository;

/*
 * ============================================================
 * WHO IS MAKING THIS REQUEST?
 * ============================================================
 *
 * Every service asks this class for the current user and only
 * reads or changes that user's data. The user is never taken
 * from the request body or URL.
 *
 * TEMPORARY: there is no login yet, so the current user is
 * always the local development user. When authentication is
 * added, only this class changes: it will return the signed-in
 * user instead.
 *
 * SideTracked must not be deployed publicly until that happens.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Transactional
    public User getCurrentUser() {

        return userRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(() ->
                        userRepository.save(
                                new User("Local Developer")
                        )
                );
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
