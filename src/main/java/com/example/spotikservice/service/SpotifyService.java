package com.example.spotikservice.service;

import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

public interface SpotifyService {
    PlaylistSimplified[] getPlaylists();

    int setAccessToken(String code);
}
