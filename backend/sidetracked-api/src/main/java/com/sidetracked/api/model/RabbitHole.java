package com.sidetracked.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "rabbit_holes")
public class RabbitHole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String world;

    private String title;

    @Column(length = 2000)
    private String hook;

    @Column(length = 2000)
    private String researchPrompt;

    private int xp;

    @Enumerated(EnumType.STRING)
    private RabbitHoleStatus status;

    private LocalDateTime discoveredAt;

    public RabbitHole() {
    }

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = RabbitHoleStatus.DISCOVERED;
        }

        if (discoveredAt == null) {
            discoveredAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getResearchPrompt() {
        return researchPrompt;
    }

    public void setResearchPrompt(String researchPrompt) {
        this.researchPrompt = researchPrompt;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public RabbitHoleStatus getStatus() {
        return status;
    }

    public void setStatus(RabbitHoleStatus status) {
        this.status = status;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(LocalDateTime discoveredAt) {
        this.discoveredAt = discoveredAt;
    }
}