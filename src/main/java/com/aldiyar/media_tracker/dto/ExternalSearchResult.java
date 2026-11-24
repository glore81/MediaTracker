package com.aldiyar.media_tracker.dto;

import com.aldiyar.media_tracker.model.MediaType;

public record ExternalSearchResult(
        String title,
        MediaType type,
        Integer totalEpisodes,
        Double externalScore,
        String synopsis
) {
}