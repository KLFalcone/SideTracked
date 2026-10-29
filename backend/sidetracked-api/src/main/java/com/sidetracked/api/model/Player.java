package com.sidetracked.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The user this game profile belongs to.
     * Set by the backend only; never sent to or read
     * from the browser.
     */
    @JsonIgnore
    @Column(nullable = false, unique = true)
    private Long userId;

    private int xp;
    private int questsCompleted;
    private int rabbitHolesCompleted;

    public Player() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getQuestsCompleted() {
        return questsCompleted;
    }

    public void setQuestsCompleted(int questsCompleted) {
        this.questsCompleted = questsCompleted;
    }

    public int getRabbitHolesCompleted() {
        return rabbitHolesCompleted;
    }

    public void setRabbitHolesCompleted(int rabbitHolesCompleted) {
        this.rabbitHolesCompleted = rabbitHolesCompleted;
    }
}