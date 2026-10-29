package com.sidetracked.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findBySourceProviderAndExternalId(
            String sourceProvider,
            String externalId
    );
}
