package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.SpotifyArtist;
import com.example.spotikservice.service.SpotifyService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
    private final SpotifyApi spotifyApi;
    private final SpotifyArtistDao artistDao;

    @Override
    public int setAccessToken(String code) {
        try {
            var execute = spotifyApi.authorizationCode(code)
                    .build()
                    .execute();
            spotifyApi.setAccessToken(execute.getAccessToken());
            return HttpStatus.SC_OK;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return HttpStatus.SC_UNAUTHORIZED;
        }
    }

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
        Arrays.stream(getPlaylistTracks(playlistId))
                .map(PlaylistTrack::getTrack)
                .filter(track -> isRussianArtist(track.getId()))
                .map(IPlaylistItem::getUri)
                .forEach(x -> {
                    try {
                        spotifyApi.removeItemsFromPlaylist(playlistId, playlistTrackToJsonArray(x))
                                .build()
                                .execute();
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        Arrays.stream(getPlaylistTracks(playlistId))
                .filter(x -> x.getTrack().getId().equals(trackId))
                .findFirst()
                .map(PlaylistTrack::getTrack)
                .map(IPlaylistItem::getUri)
                .ifPresent(x -> {
                    try {
                        spotifyApi.removeItemsFromPlaylist(playlistId, playlistTrackToJsonArray(x))
                                .build()
                                .execute();
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private JsonArray playlistTrackToJsonArray(String uri) {
        JsonObject trackObject = new JsonObject();
        trackObject.addProperty("uri", uri);

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(trackObject);
        return jsonArray;
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
                        .orElse("null")
                        .equals("Russia"));
    }

    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public TreeMap<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists() {
        return getAllNewReleases(getUserFollowedArtists());
    }

    @SneakyThrows
    private List<Artist> getUserFollowedArtists() {
        var artists = new ArrayList<Artist>();
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