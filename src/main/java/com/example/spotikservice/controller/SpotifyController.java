package com.example.spotikservice.controller;

import com.example.spotikservice.service.RabbitMqProducer;
import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyService service;
    private final RabbitMqProducer producer;
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
    public Map<String, List<AlbumSimplified>> getFollowedArtists() {
        producer.sendSongs();
        return service.getLastReleasesFromSubscribedArtists();
    }

    @GetMapping("show")
    public List<PlaylistTrack> getAllTracksFromPlaylistByCountry(@RequestParam String id, @RequestParam String code) {
        return service.getTracksFromPlaylistByCountry(id, code);
    }

    @GetMapping("account-scan")
    public List<Track> getAllTracksFromAccountByCountry(@RequestParam String code) {
        return service.getTracksFromAccountByCountry(code);
    }

    @DeleteMapping("remove-all-tracks-from-playlist")
    public void removeAllTracksByCountry(@RequestParam String id, @RequestParam String code) {
        service.removeAllTracksFromPlaylistByCountry(id, code);
    }

    @DeleteMapping("remove-track-from-playlist")
    public void removeTrackFromPlaylist(@RequestParam String id, @RequestParam String track) {
        service.removeTrackFromPlaylist(id, track);
    }
}
