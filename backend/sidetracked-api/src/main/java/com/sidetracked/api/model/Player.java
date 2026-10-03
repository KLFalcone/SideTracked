package com.sidetracked.api.model;

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

    private int xp;
    private int questsCompleted;
    private int encountersCompleted;

    public Player() {
    }

    public Long getId() {
        return id;
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

    public int getEncountersCompleted() {
        return encountersCompleted;
    }

    public void setEncountersCompleted(int encountersCompleted) {
        this.encountersCompleted = encountersCompleted;
    }
}