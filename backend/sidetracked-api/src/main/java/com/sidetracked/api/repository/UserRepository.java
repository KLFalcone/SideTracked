package com.sidetracked.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubject(String googleSubject);

    /*
     * The oldest user with no way to sign in: the local user that
     * owned data before accounts existed, if it hasn't been claimed.
     */
    Optional<User> findFirstByEmailIsNullAndGoogleSubjectIsNullOrderByIdAsc();
}
