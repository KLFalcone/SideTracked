package com.sidetracked.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "rabbit_holes")
public class RabbitHole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Primary world this rabbit hole belongs to.
     *
     * Examples:
     * REALITY & SIMULATION
     * ATOMIC AGE
     * ROADS LESS TRAVELED
     * SIGNALS & COMMUNICATIONS
     */
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

    /*
     * Rabbit holes can cross multiple subjects.
     *
     * Example:
     * The Game of Life
     *
     * Primary world:
     * REALITY & SIMULATION
     *
     * Tags:
     * TECHNOLOGY & ENGINEERING
     * SCIENCE & EXPERIMENTS
     * HISTORY & LOST KNOWLEDGE
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "rabbit_hole_tags",
        joinColumns = @JoinColumn(name = "rabbit_hole_id")
    )
    @Column(name = "tag")
    @OrderColumn(name = "tag_order")
    private List<String> tags = new ArrayList<>();

    /*
     * Useful places to begin researching the rabbit hole.
     *
     * These are intentionally stored as structured objects
     * instead of plain URLs so the frontend can display:
     *
     * Interactive Game of Life        ->
     * Original / historical source    ->
     * Accessible explanation          ->
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "rabbit_hole_sources",
        joinColumns = @JoinColumn(name = "rabbit_hole_id")
    )
    @OrderColumn(name = "source_order")
    private List<RabbitHoleSource> sources = new ArrayList<>();

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<RabbitHoleSource> getSources() {
        return sources;
    }

    public void setSources(List<RabbitHoleSource> sources) {
        this.sources = sources;
    }

    /*
     * A lightweight structured source belonging to a rabbit hole.
     *
     * Example:
     *
     * label:
     * "Interactive Game of Life"
     *
     * url:
     * "https://..."
     *
     * sourceType:
     * "INTERACTIVE"
     */
    @Embeddable
    public static class RabbitHoleSource {

        private String label;

        @Column(length = 2000)
        private String url;

        private String sourceType;

        public RabbitHoleSource() {
        }

        public RabbitHoleSource(
            String label,
            String url,
            String sourceType
        ) {
            this.label = label;
            this.url = url;
            this.sourceType = sourceType;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }
    }
}