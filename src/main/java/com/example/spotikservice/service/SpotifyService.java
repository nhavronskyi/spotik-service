package com.example.spotikservice.service;

import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;
import java.util.Map;

public interface SpotifyService {
    PlaylistSimplified[] getPlaylists();

    int setAccessToken(String code);

    Map<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists();

    List<PlaylistTrack> getRussianTracks(String id);

    void removeAllRussianTracksFromPlaylist(String playlistId);

    void removeTrackFromPlaylist(String playlistId, String trackId);
}
