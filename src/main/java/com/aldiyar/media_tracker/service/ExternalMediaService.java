package com.aldiyar.media_tracker.service;

import com.aldiyar.media_tracker.dto.ExternalSearchResult;
import com.aldiyar.media_tracker.model.MediaItem;
import com.aldiyar.media_tracker.model.MediaType;
import com.aldiyar.media_tracker.model.WatchStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
public class ExternalMediaService {

    private final RestClient restClient;
    private final MediaItemService mediaItemService;

    public ExternalMediaService(MediaItemService mediaItemService) {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "MediaTrackerApp/1.0")
                .build();
        this.mediaItemService = mediaItemService;
    }

    public List<ExternalSearchResult> search(String query, MediaType type) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр query не должен быть пустым");
        }

        if (type == MediaType.ANIME) {
            return searchAnimeFromKitsu(query);
        }
        return searchShowsFromTvMaze(query, type != null ? type : MediaType.SERIES);
    }

    public MediaItem importToMyList(ExternalSearchResult selected, WatchStatus status) {
        MediaItem item = new MediaItem();
        item.setTitle(selected.title());
        item.setType(selected.type());
        item.setStatus(status != null ? status : WatchStatus.PLANNED);
        item.setEpisodesWatched(0);
        item.setTotalEpisodes(selected.totalEpisodes());
        item.setNotes(selected.synopsis());
        return mediaItemService.create(item);
    }

    private List<ExternalSearchResult> searchAnimeFromKitsu(String query) {
        try {
            KitsuResponse response = restClient.get()
                    .uri("https://kitsu.io/api/edge/anime?filter[text]={query}&page[limit]=5", query)
                    .retrieve()
                    .body(KitsuResponse.class);

            if (response == null || response.data() == null) {
                return List.of();
            }

            return response.data().stream()
                    .map(item -> {
                        KitsuAttributes attr = item.attributes();
                        Double score = null;
                        if (attr.averageRating() != null) {
                            // Kitsu отдаёт рейтинг по 100-балльной шкале (например, "84.5"), переводим в 10-балльную
                            score = Math.round(Double.parseDouble(attr.averageRating())) / 10.0;
                        }
                        return new ExternalSearchResult(
                                attr.canonicalTitle(),
                                MediaType.ANIME,
                                attr.episodeCount(),
                                score,
                                trimText(attr.synopsis())
                        );
                    })
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ошибка Kitsu API: " + e.getMessage());
        }
    }

    private List<ExternalSearchResult> searchShowsFromTvMaze(String query, MediaType type) {
        try {
            TvMazeItem[] response = restClient.get()
                    .uri("https://api.tvmaze.com/search/shows?q={query}", query)
                    .retrieve()
                    .body(TvMazeItem[].class);

            if (response == null) {
                return List.of();
            }

            return Arrays.stream(response)
                    .limit(5)
                    .map(item -> {
                        TvMazeShow show = item.show();
                        Double score = show.rating() != null ? show.rating().average() : null;
                        String cleanSummary = show.summary() != null
                                ? show.summary().replaceAll("<[^>]*>", "") : null;
                        return new ExternalSearchResult(
                                show.name(),
                                type,
                                null,
                                score,
                                trimText(cleanSummary)
                        );
                    })
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ошибка TVMaze API: " + e.getMessage());
        }
    }

    private String trimText(String text) {
        if (text == null || text.isBlank()) return null;
        return text.length() > 300 ? text.substring(0, 297) + "..." : text;
    }

    private record KitsuResponse(List<KitsuItem> data) {}
    private record KitsuItem(KitsuAttributes attributes) {}
    private record KitsuAttributes(String canonicalTitle, Integer episodeCount, String averageRating, String synopsis) {}

    private record TvMazeItem(TvMazeShow show) {}
    private record TvMazeShow(String name, TvMazeRating rating, String summary) {}
    private record TvMazeRating(Double average) {}
}