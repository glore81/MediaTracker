package com.aldiyar.media_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_items")
public class MediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus status;

    private Integer rating;

    @Column(name = "episodes_watched")
    private Integer episodesWatched = 0;

    @Column(name = "total_episodes")
    private Integer totalEpisodes;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MediaItem() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }

    public WatchStatus getStatus() { return status; }
    public void setStatus(WatchStatus status) { this.status = status; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getEpisodesWatched() { return episodesWatched; }
    public void setEpisodesWatched(Integer episodesWatched) { this.episodesWatched = episodesWatched; }

    public Integer getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(Integer totalEpisodes) { this.totalEpisodes = totalEpisodes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}