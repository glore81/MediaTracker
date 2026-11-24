package com.aldiyar.media_tracker.service;

import com.aldiyar.media_tracker.model.MediaItem;
import com.aldiyar.media_tracker.model.MediaType;
import com.aldiyar.media_tracker.model.WatchStatus;
import com.aldiyar.media_tracker.repository.MediaItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaItemServiceTest {

    @Mock
    private MediaItemRepository repository;

    @InjectMocks
    private MediaItemService service;

    private MediaItem movie;
    private MediaItem series;

    @BeforeEach
    void setUp() {
        movie = new MediaItem();
        movie.setId(1L);
        movie.setType(MediaType.MOVIE);
        movie.setStatus(WatchStatus.PLANNED);

        series = new MediaItem();
        series.setId(2L);
        series.setType(MediaType.SERIES);
        series.setStatus(WatchStatus.PLANNED);
        series.setEpisodesWatched(0);
        series.setTotalEpisodes(2);
    }

    @Test
    void watchNextEpisode_forMovie_marksCompletedImmediately() {
        when(repository.findById(1L)).thenReturn(Optional.of(movie));
        when(repository.save(any(MediaItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MediaItem result = service.watchNextEpisode(1L);

        assertThat(result.getStatus()).isEqualTo(WatchStatus.COMPLETED);
    }

    @Test
    void watchNextEpisode_forMovie_alreadyCompleted_throwsBadRequest() {
        movie.setStatus(WatchStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> service.watchNextEpisode(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("уже просмотрен");
    }

    @Test
    void watchNextEpisode_forSeries_incrementsAndSwitchesToWatching() {
        when(repository.findById(2L)).thenReturn(Optional.of(series));
        when(repository.save(any(MediaItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MediaItem result = service.watchNextEpisode(2L);

        assertThat(result.getEpisodesWatched()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(WatchStatus.WATCHING);
    }

    @Test
    void watchNextEpisode_forSeries_reachesLastEpisode_marksCompleted() {
        series.setEpisodesWatched(1);
        when(repository.findById(2L)).thenReturn(Optional.of(series));
        when(repository.save(any(MediaItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MediaItem result = service.watchNextEpisode(2L);

        assertThat(result.getEpisodesWatched()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(WatchStatus.COMPLETED);
    }

    @Test
    void watchNextEpisode_forSeries_exceedsTotalEpisodes_throwsBadRequest() {
        series.setEpisodesWatched(2);
        when(repository.findById(2L)).thenReturn(Optional.of(series));

        assertThatThrownBy(() -> service.watchNextEpisode(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Все серии уже просмотрены");
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не найден");
    }
}