package com.sidetracked.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/*
 * A Rabbit Hole's shared content: the "book on the shelf".
 *
 * Each topic exists once and is shared by every user.
 * A user's own progress with a topic lives in UserRabbitHole.
 */
@Entity
@Table(
    name = "topics",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_topics_source",
        columnNames = { "source_provider", "external_id" }
    )
)
public class Topic {

    /*
     * Where a topic came from. Curated topics are written by hand;
     * later providers (e.g. Wikipedia) will add their own value.
     */
    public static final String CURATED = "CURATED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * sourceProvider + externalId identify a topic at its source,
     * so the same discovery can never be stored twice.
     */
    @Column(nullable = false)
    private String sourceProvider;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String title;

    /*
     * Slug of the primary World this topic belongs to,
     * e.g. "atomic-age". See WorldCatalog.
     */
    private String world;

    /*
     * Slugs of other Worlds this topic connects to.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "topic_worlds",
        joinColumns = @JoinColumn(name = "topic_id")
    )
    @Column(name = "world_slug", nullable = false)
    @OrderColumn(name = "world_order")
    private List<String> relatedWorlds = new ArrayList<>();

    @Column(length = 2000)
    private String hook;

    @Column(length = 2000)
    private String researchPrompt;

    private int xp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /*
     * Free-form themes that aren't Worlds,
     * e.g. "UNSOLVED & UNEXPLAINED".
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "topic_tags",
        joinColumns = @JoinColumn(name = "topic_id")
    )
    @Column(name = "tag")
    @OrderColumn(name = "tag_order")
    private List<String> tags = new ArrayList<>();

    /*
     * Credible, topic-specific places to start researching.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "topic_sources",
        joinColumns = @JoinColumn(name = "topic_id")
    )
    @OrderColumn(name = "source_order")
    private List<TopicSource> sources = new ArrayList<>();

    public Topic() {
    }

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = Times.nowUtc();
        }
    }

    /*
     * "THE WOW! SIGNAL" -> "the-wow-signal"
     *
     * Must match the external_id expression in V3__topic_catalog.sql.
     */
    public static String slugify(String title) {

        return title
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /*
     * Copies the content of another topic into this one,
     * keeping this topic's id and creation date.
     */
    public void copyContentFrom(Topic other) {

        this.sourceProvider = other.sourceProvider;
        this.externalId = other.externalId;
        this.title = other.title;
        this.world = other.world;
        this.hook = other.hook;
        this.researchPrompt = other.researchPrompt;
        this.xp = other.xp;

        this.relatedWorlds.clear();
        this.relatedWorlds.addAll(other.relatedWorlds);

        this.tags.clear();
        this.tags.addAll(other.tags);

        this.sources.clear();
        this.sources.addAll(other.sources);
    }

    public Long getId() {
        return id;
    }

    public String getSourceProvider() {
        return sourceProvider;
    }

    public void setSourceProvider(String sourceProvider) {
        this.sourceProvider = sourceProvider;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public List<String> getRelatedWorlds() {
        return relatedWorlds;
    }

    public void setRelatedWorlds(List<String> relatedWorlds) {
        this.relatedWorlds = relatedWorlds;
    }

    /*
     * True if the topic can be discovered from this World,
     * either as its primary World or a related one.
     */
    public boolean belongsTo(String worldSlug) {

        return worldSlug.equals(world)
                || relatedWorlds.contains(worldSlug);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<TopicSource> getSources() {
        return sources;
    }

    public void setSources(List<TopicSource> sources) {
        this.sources = sources;
    }
}
