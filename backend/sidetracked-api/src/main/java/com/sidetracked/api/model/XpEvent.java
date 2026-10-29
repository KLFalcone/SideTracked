package com.sidetracked.api.model;

import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/*
 * One line on a user's XP "bank statement".
 *
 * Example: +35 "THE REACTOR THAT BUILT ITSELF" at 2026-10-29 09:14
 *
 * The database allows only one event per (user, sourceType,
 * sourceId), so the same completion can never pay out twice.
 */
@Entity
@Table(
    name = "xp_events",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_xp_events_source",
        columnNames = { "user_id", "source_type", "source_id" }
    )
)
public class XpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private XpSourceType sourceType;

    @Column(nullable = false)
    private Long sourceId;

    /* Positive when XP is earned, negative when it is taken back. */
    private int amount;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /* True when the timestamp was filled in by a migration. */
    private boolean backfilled;

    public XpEvent() {
    }

    public XpEvent(
            Long userId,
            XpSourceType sourceType,
            Long sourceId,
            int amount,
            String label) {

        this.userId = userId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.amount = amount;
        this.label = label;
        this.createdAt = Times.nowUtc();
        this.backfilled = false;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public XpSourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public int getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    /*
     * Sent as an Instant so the browser knows it's UTC.
     */
    public Instant getCreatedAt() {
        return Times.toInstant(createdAt);
    }

    public boolean isBackfilled() {
        return backfilled;
    }
}
