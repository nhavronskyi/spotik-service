package com.example.spotikservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CallbackController {

    private final SpotifyApi spotifyApi;

    @SneakyThrows
    @GetMapping
    public PlaylistSimplified[] getAllPlaylists(@RequestParam String code){
        AuthorizationCodeCredentials execute = spotifyApi.authorizationCode(code)
                .build().execute();

        String accessToken = execute.getAccessToken();
        spotifyApi.setAccessToken(accessToken);
        return spotifyApi.getListOfCurrentUsersPlaylists()
                .build().execute().getItems();
    }
}
