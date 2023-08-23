package com.example.spotikservice.service;

import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.List;
import java.util.TreeMap;

public interface SpotifyService {
    String getAccountId();

    List<PlaylistSimplified> getPlaylists();

    List<Album> getAlbums();

    List<Track> getSavedSongs();

    List<TrackSimplified> getTracksFromAlbumByCountry(String albumId, String code);

    TreeMap<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists();

    List<PlaylistTrack> getTracksFromPlaylistByCountry(String id, String code);

    List<Track> getTracksFromAccountByCountry(String code);

    void removeAllTracksFromPlaylistByCountry(String playlistId, String code);

    void removeTrackFromPlaylist(String playlistId, String trackId);
}
