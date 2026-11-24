package com.aldiyar.media_tracker.controller;

import com.aldiyar.media_tracker.dto.ExternalSearchResult;
import com.aldiyar.media_tracker.model.MediaItem;
import com.aldiyar.media_tracker.model.MediaType;
import com.aldiyar.media_tracker.model.WatchStatus;
import com.aldiyar.media_tracker.service.ExternalMediaService;
import com.aldiyar.media_tracker.service.MediaItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaItemController {

    private final MediaItemService service;
    private final ExternalMediaService externalService;

    public MediaItemController(MediaItemService service, ExternalMediaService externalService) {
        this.service = service;
        this.externalService = externalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaItem create(@RequestBody MediaItem item) {
        return service.create(item);
    }

    @GetMapping
    public List<MediaItem> getAll(
            @RequestParam(required = false) MediaType type,
            @RequestParam(required = false) WatchStatus status,
            @RequestParam(required = false) String search
    ) {
        return service.getAll(type, status, search);
    }

    @GetMapping("/{id}")
    public MediaItem getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public MediaItem update(@PathVariable Long id, @RequestBody MediaItem item) {
        return service.update(id, item);
    }

    @PatchMapping("/{id}/watch")
    public MediaItem watchNextEpisode(@PathVariable Long id) {
        return service.watchNextEpisode(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/search-external")
    public List<ExternalSearchResult> searchExternal(
            @RequestParam String query,
            @RequestParam(defaultValue = "ANIME") MediaType type
    ) {
        return externalService.search(query, type);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaItem importFromSearch(
            @RequestBody ExternalSearchResult selected,
            @RequestParam(defaultValue = "PLANNED") WatchStatus status
    ) {
        return externalService.importToMyList(selected, status);
    }
}