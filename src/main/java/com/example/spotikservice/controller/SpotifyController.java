package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyService service;

    @GetMapping("playlists")
    public List<PlaylistSimplified> getPlaylists() {
        return service.getPlaylists();
    }

    @GetMapping("albums")
    public List<Album> getAlbums() {
        return service.getAlbums();
    }

    @GetMapping("songs")
    public List<Track> getSavedSongs() {
        return service.getSavedSongs();
    }

    @GetMapping("last-releases")
    public TreeMap<String, List<AlbumSimplified>> getFollowedArtists() {
        return service.getLastReleasesFromSubscribedArtists();
    }

    @GetMapping("show-ru")
    public List<PlaylistTrack> checkIfThereAreRussianTracksAdded(@RequestParam String id) {
        return service.getRussianTracksFromPlaylist(id);
    }

    @GetMapping("account-scan")
    public List<Track> checkIfThereAreRussianTracksAddedInWholeAccount() {
        return service.getRussianTracksFromAccount();
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
