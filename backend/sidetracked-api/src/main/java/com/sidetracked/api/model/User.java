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
 * Everything personal (player stats, quests, Rabbit Hole history)
 * points back to a user. A user signs in with an email + password,
 * with Google, or both. This entity is never sent to the browser.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    private LocalDateTime createdAt;

    /* Unique, stored lowercase. Null until a password is set. */
    private String email;

    /* BCrypt hash; the password itself is never stored. */
    private String passwordHash;

    /* Google's permanent id for this person. */
    private String googleSubject;

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

    /*
     * True for a user with no way to sign in yet, like the local
     * user that owned data before accounts existed.
     */
    public boolean hasNoLogin() {
        return email == null && googleSubject == null;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(String googleSubject) {
        this.googleSubject = googleSubject;
    }
}
