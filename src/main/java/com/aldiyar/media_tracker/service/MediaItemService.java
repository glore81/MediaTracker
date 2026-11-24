package com.aldiyar.media_tracker.service;

import com.aldiyar.media_tracker.model.MediaItem;
import com.aldiyar.media_tracker.model.MediaType;
import com.aldiyar.media_tracker.model.WatchStatus;
import com.aldiyar.media_tracker.repository.MediaItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MediaItemService {

    private final MediaItemRepository repository;

    public MediaItemService(MediaItemRepository repository) {
        this.repository = repository;
    }

    public MediaItem create(MediaItem item) {
        if (item.getType() == MediaType.MOVIE) {
            item.setEpisodesWatched(null);
            item.setTotalEpisodes(null);
        }
        return repository.save(item);
    }

    public List<MediaItem> getAll(MediaType type, WatchStatus status, String search) {
        if (search != null && !search.isBlank()) {
            return repository.findByTitleContainingIgnoreCase(search);
        }
        if (type != null) {
            return repository.findByType(type);
        }
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public MediaItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тайтл не найден"));
    }

    public MediaItem update(Long id, MediaItem newData) {
        MediaItem existing = getById(id);
        existing.setTitle(newData.getTitle());
        existing.setType(newData.getType());
        existing.setStatus(newData.getStatus());
        existing.setNotes(newData.getNotes());

        if (newData.getType() == MediaType.MOVIE) {
            existing.setEpisodesWatched(null);
            existing.setTotalEpisodes(null);
        } else {
            existing.setEpisodesWatched(newData.getEpisodesWatched());
            existing.setTotalEpisodes(newData.getTotalEpisodes());
        }

        return repository.save(existing);
    }

    public MediaItem watchNextEpisode(Long id) {
        MediaItem item = getById(id);

        if (item.getType() == MediaType.MOVIE) {
            if (item.getStatus() == WatchStatus.COMPLETED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Фильм уже просмотрен");
            }
            item.setStatus(WatchStatus.COMPLETED);
            return repository.save(item);
        }

        int watched = (item.getEpisodesWatched() == null ? 0 : item.getEpisodesWatched()) + 1;

        if (item.getTotalEpisodes() != null && watched > item.getTotalEpisodes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Все серии уже просмотрены");
        }

        item.setEpisodesWatched(watched);

        if (item.getTotalEpisodes() != null && watched == item.getTotalEpisodes()) {
            item.setStatus(WatchStatus.COMPLETED);
        } else if (item.getStatus() == WatchStatus.PLANNED) {
            item.setStatus(WatchStatus.WATCHING);
        }

        return repository.save(item);
    }

    public void delete(Long id) {
        MediaItem existing = getById(id);
        repository.delete(existing);
    }
}