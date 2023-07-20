package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CallbackController {
    private final SpotifyService service;

    @SneakyThrows
    @GetMapping
    public int setAccessToken(@RequestParam String code) {
        return service.setAccessToken(code);
    }

    @GetMapping("playlists")
    public PlaylistSimplified[] getPlaylists() {
        return service.getPlaylists();
    }

    @GetMapping("show_ru")
    public List<PlaylistTrack> checkIfThereAreRussianTracksAdded(@RequestParam String id) {
        return service.getRussianTracks(id);
    }

    @DeleteMapping("delete_all_ru_tracks")
    public void deleteAllRuTracks(@RequestParam String id) {
        service.deleteAllRussianTracksFromPlaylist(id);
    }

    @DeleteMapping("delete_track_from_playlist")
    public void deleteTrackFromPlaylist(@RequestParam(name = "playlist_id") String playlistId, @RequestParam(name = "track_id") String trackId) {
        service.removeTrackFromPlaylist(playlistId, trackId);
    }
}
