package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;
import java.util.TreeMap;

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

    @GetMapping("songs")
    public TreeMap<String, List<AlbumSimplified>> getFollowedArtists() {
        return service.getLastReleasesFromSubscribedArtists();
    }

    @GetMapping("show_ru")
    public List<PlaylistTrack> checkIfThereAreRussianTracksAdded(@RequestParam String id) {
        return service.getRussianTracks(id);
    }

    @DeleteMapping("remove_all_ru_tracks")
    public void removeAllRuTracks(@RequestParam String id) {
        service.removeAllRussianTracksFromPlaylist(id);
    }

    @DeleteMapping("remove_track_from_playlist")
    public void removeTrackFromPlaylist(@RequestParam(name = "playlist_id") String playlistId, @RequestParam(name = "track_id") String trackId) {
        service.removeTrackFromPlaylist(playlistId, trackId);
    }
}
