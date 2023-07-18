package com.example.spotikservice.controller;

import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

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
    public TreeMap<String, List<AlbumSimplified>> getFollowedArtists(){
        return service.getLastReleasesFromSubscribedArtists();
    }
}
