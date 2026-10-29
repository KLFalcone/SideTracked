package com.sidetracked.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.UserRabbitHole;

/*
 * A user's Rabbit Holes are private, so every lookup includes
 * the owner's user id. Use these methods instead of
 * findAll()/findById().
 */
public interface UserRabbitHoleRepository
        extends JpaRepository<UserRabbitHole, Long> {

    List<UserRabbitHole> findByUserIdOrderByDiscoveredAtDesc(
            Long userId
    );

    Optional<UserRabbitHole> findByUserIdAndTopicId(
            Long userId,
            Long topicId
    );
}
