package com.sidetracked.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.QuestCompletion;

/*
 * Completions are only looked up through a quest that has
 * already been checked to belong to the current user.
 */
public interface QuestCompletionRepository
        extends JpaRepository<QuestCompletion, Long> {

    List<QuestCompletion> findByQuestId(Long questId);
}
