package com.example.spotikservice.service.impl;

import com.example.spotikservice.props.SpotifyProps;
import com.example.spotikservice.service.AuthService;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;

import java.net.URI;

@Service
public class AuthServiceImpl implements AuthService {
    private final SpotifyApi spotifyApi;

    public AuthServiceImpl(SpotifyProps spotifyProps) {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyProps.id())
                .setClientSecret(spotifyProps.secret())
                .setRedirectUri(URI.create(spotifyProps.uri()))
                .build();
    }

    @Override
    public String authorize() {
        return spotifyApi
                .authorizationCodeUri()
                .build()
                .execute()
                .toString();
    }
}
