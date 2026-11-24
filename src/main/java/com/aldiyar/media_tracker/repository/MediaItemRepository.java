package com.aldiyar.media_tracker.repository;

import com.aldiyar.media_tracker.model.MediaItem;
import com.aldiyar.media_tracker.model.MediaType;
import com.aldiyar.media_tracker.model.WatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {
    List<MediaItem> findByType(MediaType type);
    List<MediaItem> findByStatus(WatchStatus status);
    List<MediaItem> findByTitleContainingIgnoreCase(String title);
}