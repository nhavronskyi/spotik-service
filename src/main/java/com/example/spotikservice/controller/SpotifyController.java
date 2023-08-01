package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyService service;

    @GetMapping("playlists")
    public PlaylistSimplified[] getPlaylists() {
        return service.getPlaylists();
    }

    @GetMapping("songs")
    public Map<String, List<AlbumSimplified>> getFollowedArtists() {
        return service.getLastReleasesFromSubscribedArtists();
    }

    @GetMapping("show-ru")
    public List<PlaylistTrack> checkIfThereAreRussianTracksAdded(@RequestParam String id) {
        return service.getRussianTracks(id);
    }

    @DeleteMapping("remove-all-ru-tracks")
    public void removeAllRuTracks(@RequestParam String id) {
        service.removeAllRussianTracksFromPlaylist(id);
    }

    @DeleteMapping("remove-track-from-playlist")
    public void removeTrackFromPlaylist(@RequestParam(name = "playlist-id") String playlistId, @RequestParam(name = "track-id") String trackId) {
        service.removeTrackFromPlaylist(playlistId, trackId);
    }
}
