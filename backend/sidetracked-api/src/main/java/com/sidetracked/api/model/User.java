package com.sidetracked.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/*
 * A person using SideTracked.
 *
 * Everything personal (player stats, quests, and later
 * Rabbit Hole history) points back to a user.
 *
 * Login details will be added here when authentication
 * is built.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    private LocalDateTime createdAt;

    public User() {
    }

    public User(String displayName) {
        this.displayName = displayName;
    }

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = Times.nowUtc();
        }
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
