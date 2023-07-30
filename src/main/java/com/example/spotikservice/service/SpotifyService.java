package com.example.spotikservice.service;

import jakarta.servlet.http.HttpServletRequest;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.List;
import java.util.Map;

public interface SpotifyService {
    PlaylistSimplified[] getPlaylists(HttpServletRequest request);

    Map<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists(HttpServletRequest request);

    List<PlaylistTrack> getRussianTracks(String id, HttpServletRequest request);

    void removeAllRussianTracksFromPlaylist(String playlistId, HttpServletRequest request);

    void removeTrackFromPlaylist(HttpServletRequest request, String playlistId, String trackId);
}
