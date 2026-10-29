package com.sidetracked.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.Quest;

/*
 * Quests are private, so every lookup includes the owner's
 * user id. Use these methods instead of findAll()/findById().
 */
public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findByUserId(Long userId);

    Optional<Quest> findByIdAndUserId(Long id, Long userId);
}
