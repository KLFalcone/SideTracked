package com.sidetracked.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/*
 * One user's journey down one topic: the "checkout card".
 *
 * Holds everything personal: status, journal note, and dates.
 * The database allows only one per user per topic, so the same
 * topic can never be duplicated in someone's history.
 */
@Entity
@Table(
    name = "user_rabbit_holes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_rabbit_holes_user_topic",
        columnNames = { "user_id", "topic_id" }
    )
)
public class UserRabbitHole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RabbitHoleStatus status;

    /*
     * "One thing I learned..." journal note.
     */
    @Column(length = 2000)
    private String reflection;

    @Column(nullable = false)
    private LocalDateTime discoveredAt;

    private LocalDateTime completedAt;

    public UserRabbitHole() {
    }

    public UserRabbitHole(Long userId, Topic topic) {

        this.userId = userId;
        this.topic = topic;
        this.status = RabbitHoleStatus.DISCOVERED;
        this.discoveredAt = Times.nowUtc();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Topic getTopic() {
        return topic;
    }

    public RabbitHoleStatus getStatus() {
        return status;
    }

    public void setStatus(RabbitHoleStatus status) {
        this.status = status;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
