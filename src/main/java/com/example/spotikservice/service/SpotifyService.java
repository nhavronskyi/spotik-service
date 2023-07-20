package com.example.spotikservice.service;

import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;

public interface SpotifyService {
    PlaylistSimplified[] getPlaylists();

    int setAccessToken(String code);

    List<PlaylistTrack> getRussianTracks(String id);

    void deleteAllRussianTracksFromPlaylist(String playlistId);

    void removeTrackFromPlaylist(String playlistId, String trackId);
}
