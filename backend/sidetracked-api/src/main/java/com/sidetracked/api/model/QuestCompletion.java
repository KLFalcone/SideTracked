package com.sidetracked.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * One time a quest was completed.
 *
 * A regular quest completes once. Repeating quests (for example
 * medication every 8 hours) will have one completion per time,
 * which is what their timeline will be built from.
 */
@Entity
@Table(name = "quest_completions")
public class QuestCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questId;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    /* True when the timestamp was filled in by a migration. */
    private boolean backfilled;

    public QuestCompletion() {
    }

    public QuestCompletion(Long questId) {

        this.questId = questId;
        this.completedAt = Times.nowUtc();
        this.backfilled = false;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestId() {
        return questId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public boolean isBackfilled() {
        return backfilled;
    }
}
