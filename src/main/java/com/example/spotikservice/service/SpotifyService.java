package com.example.spotikservice.service;

import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.List;
import java.util.TreeMap;

public interface SpotifyService {
    List<PlaylistSimplified> getPlaylists();

    List<Album> getAlbums();

    List<Track> getSavedSongs();

    List<TrackSimplified> getRussianTracksFromAlbum(String albumId);

    TreeMap<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists();

    List<PlaylistTrack> getRussianTracksFromPlaylist(String id);

    List<Track> getRussianTracksFromAccount();

    void removeAllRussianTracksFromPlaylist(String playlistId);

    void removeTrackFromPlaylist(String playlistId, String trackId);
}
