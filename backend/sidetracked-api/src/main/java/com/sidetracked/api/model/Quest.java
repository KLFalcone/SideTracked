package com.sidetracked.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The user who owns this quest.
     * Set by the backend only; never sent to or read
     * from the browser.
     */
    @JsonIgnore
    @Column(nullable = false)
    private Long userId;

    /*
     * Limits match the database columns, so an overly long
     * entry is rejected with a clear error instead of failing
     * when it is saved.
     */
    @NotBlank(message = "A quest needs a title.")
    @Size(max = 255, message = "Quest titles can be at most 255 characters.")
    private String title;

    @Size(max = 255, message = "Quest notes can be at most 255 characters.")
    private String description;

    @Size(max = 255)
    private String type;

    @Size(max = 255)
    private String priority;
    private boolean completed;
    private int xp;

    public Quest() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}