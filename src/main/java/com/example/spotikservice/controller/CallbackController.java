package com.example.spotikservice.controller;

import com.example.spotikservice.service.AuthService;
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
    private final SpotifyService spotifyService;
    private final AuthService authService;

    @GetMapping
    public int setAccessToken(@RequestParam String code, HttpServletResponse response) {
        return authService.setAccessToken(code, response);
    }

    @GetMapping("playlists")
    public PlaylistSimplified[] getPlaylists(HttpServletRequest request) {
        return spotifyService.getPlaylists(request);
    }

    @GetMapping("songs")
    public Map<String, List<AlbumSimplified>> getFollowedArtists(HttpServletRequest request) {
        return spotifyService.getLastReleasesFromSubscribedArtists(request);
    }

    @GetMapping("show-ru")
    public List<PlaylistTrack> checkIfThereAreRussianTracksAdded(HttpServletRequest request, @RequestParam String id) {
        return spotifyService.getRussianTracks(id, request);
    }

    @DeleteMapping("remove-all-ru-tracks")
    public void removeAllRuTracks(HttpServletRequest request, @RequestParam String id) {
        spotifyService.removeAllRussianTracksFromPlaylist(id, request);
    }

    @DeleteMapping("remove-track-from-playlist")
    public void removeTrackFromPlaylist(HttpServletRequest request, @RequestParam(name = "playlist-id") String playlistId, @RequestParam(name = "track-id") String trackId) {
        spotifyService.removeTrackFromPlaylist(request, playlistId, trackId);
    }
}
