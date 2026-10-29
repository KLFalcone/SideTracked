package com.sidetracked.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.QuestCompletion;

/*
 * Completions are only looked up through quests that have
 * already been checked to belong to the current user.
 */
public interface QuestCompletionRepository
        extends JpaRepository<QuestCompletion, Long> {

    List<QuestCompletion> findByQuestId(Long questId);

    List<QuestCompletion> findByQuestIdIn(Collection<Long> questIds);

    Optional<QuestCompletion> findTopByQuestIdOrderByCompletedAtDesc(
            Long questId
    );
}
