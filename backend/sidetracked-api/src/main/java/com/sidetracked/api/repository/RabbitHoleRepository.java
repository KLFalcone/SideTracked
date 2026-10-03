package com.sidetracked.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.RabbitHole;
import com.sidetracked.api.model.RabbitHoleStatus;

public interface RabbitHoleRepository
        extends JpaRepository<RabbitHole, Long> {

    List<RabbitHole> findByStatusOrderByDiscoveredAtDesc(
            RabbitHoleStatus status
    );

    List<RabbitHole> findAllByOrderByDiscoveredAtDesc();
}