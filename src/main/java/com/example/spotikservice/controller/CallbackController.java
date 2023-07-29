package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class CallbackController {
    private final SpotifyService service;

    @GetMapping
    public int setAccessToken(@RequestParam String code, HttpServletResponse response) {
        return service.setAccessToken(code, response);
    }

    @GetMapping("playlists")
    public PlaylistSimplified[] getPlaylists(HttpServletRequest request) {
        return service.getPlaylists(request);
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
