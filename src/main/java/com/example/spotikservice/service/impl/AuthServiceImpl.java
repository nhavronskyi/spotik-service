package com.example.spotikservice.service.impl;

import com.example.spotikservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SpotifyApi spotifyApi;

    @Override
    public String authorize() {
        return spotifyApi
                .authorizationCodeUri()
                .build()
                .execute()
                .toString();
    }
}
