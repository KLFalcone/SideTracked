package com.sidetracked.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}