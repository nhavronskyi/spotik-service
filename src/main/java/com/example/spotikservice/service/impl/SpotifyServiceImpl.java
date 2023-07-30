package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.SpotifyArtist;
import com.example.spotikservice.service.SpotifyService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collector;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
    private final SpotifyApi spotifyApi;
    private final SpotifyArtistDao artistDao;

    @Override
    @SneakyThrows
    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public PlaylistSimplified[] getPlaylists() {
        return spotifyApi.getListOfCurrentUsersPlaylists()
                .build()
                .execute()
                .getItems();
    }

    public List<PlaylistTrack> getRussianTracks(String playlistId) {
        var tracks = getPlaylistTracks(playlistId);
        return Arrays.stream(tracks)
                .filter(track -> isRussianArtist(track.getTrack().getId()))
                .toList();
    }

    public void removeAllRussianTracksFromPlaylist(String playlistId) {
        var json = Arrays.stream(getPlaylistTracks(playlistId))
                .map(PlaylistTrack::getTrack)
                .filter(track -> isRussianArtist(track.getId()))
                .map(this::mapToJsonObject)
                .collect(Collector.of(
                        JsonArray::new,
                        JsonArray::add,
                        (jsonElements, jsonElements2) -> {
                            jsonElements.addAll(jsonElements2);
                            return jsonElements;
                        }));
        removeFromPlaylist(playlistId, json);
    }

    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        var json = Arrays.stream(getPlaylistTracks(playlistId))
                .map(PlaylistTrack::getTrack)
                .filter(x -> x.getId().equals(trackId))
                .findFirst()
                .map(this::mapToJsonObject)
                .map(obj -> {
                    JsonArray jsonArray = new JsonArray();
                    jsonArray.add(obj);
                    return jsonArray;
                })
                .orElseGet(JsonArray::new);
        removeFromPlaylist(playlistId, json);
    }

    private JsonObject mapToJsonObject(IPlaylistItem iPlaylistItem) {
        JsonObject trackObject = new JsonObject();
        trackObject.addProperty("uri", iPlaylistItem.getUri());
        return trackObject;
    }

    @SneakyThrows
    private void removeFromPlaylist(String playlistId, JsonArray jsonArray) {
        spotifyApi.removeItemsFromPlaylist(playlistId, jsonArray)
                .build()
                .execute();
    }

    @SneakyThrows
    private PlaylistTrack[] getPlaylistTracks(String playlistId) {
        return spotifyApi.getPlaylist(playlistId).build().execute().getTracks().getItems();
    }

    @SneakyThrows
    private boolean isRussianArtist(String trackId) {
        var artists = spotifyApi.getTrack(trackId)
                .build()
                .execute()
                .getArtists();
        return Arrays.stream(artists)
                .anyMatch(artist -> artistDao.findById(artist.getId())
                        .map(SpotifyArtist::getCountry)
                        .orElse("unknown")
                        .equals("Russia"));
    }

    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public Map<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists() {
        return getAllNewReleases(getUserFollowedArtists());
    }

    @SneakyThrows
    private List<Artist> getUserFollowedArtists() {
        List<Artist> artists = new LinkedList<>();
        String cursor = null;

        do {
            var followedArtistsPage = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
                    .limit(50)
                    .after(Optional.ofNullable(cursor).orElse("0"))
                    .build()
                    .execute();

            if (followedArtistsPage != null) {
                artists.addAll(Arrays.asList(followedArtistsPage.getItems()));
                cursor = followedArtistsPage.getCursors()[0].getAfter();
            } else {
                break;
            }
        } while (cursor != null);

        return artists;
    }

    @SneakyThrows
    private TreeMap<String, List<AlbumSimplified>> getAllNewReleases(List<Artist> artists) {
        var map = new TreeMap<String, List<AlbumSimplified>>();
        for (Artist artist : artists) {
            var songs = Arrays.stream(spotifyApi.getArtistsAlbums(artist.getId())
                            .build()
                            .execute()
                            .getItems())
                    .filter(x -> {
                        try {
                            return LocalDate.parse(x.getReleaseDate()).isAfter(LocalDate.now().minusMonths(1));
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    }).toList();
            if (songs.size() != 0) {
                map.put(artist.getName(), songs);
            }
        }
        return map;
    }
}