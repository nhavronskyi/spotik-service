package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.SpotifyArtist;
import com.example.spotikservice.entities.User;
import com.example.spotikservice.exception.UserUnauthorizedException;
import com.example.spotikservice.service.SpotifyService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collector;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
    private final SpotifyApi spotifyApi;
    private final SpotifyArtistDao artistDao;
    private final UserDao userDao;

    @Override
    public int setAccessToken(String code, HttpServletResponse response) {
        try {
            var execute = spotifyApi.authorizationCode(code)
                    .build()
                    .execute();

            String accessToken = execute.getAccessToken();
            String refreshToken = execute.getRefreshToken();
            spotifyApi.setAccessToken(accessToken);

            String userId = spotifyApi.getCurrentUsersProfile().build().execute().getId();
            User user = new User(userId, accessToken, refreshToken);
            userDao.save(user);

            Cookie cookie = new Cookie("user_id", userId);
            cookie.setPath("/"); // Параметр "Path" вказує шлях, на якому доступне кукі (у цьому випадку доступне на всій домені)
            response.addCookie(cookie);

            return HttpStatus.SC_OK;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return HttpStatus.SC_UNAUTHORIZED;
        }
    }

    @Override
    @SneakyThrows
    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public PlaylistSimplified[] getPlaylists(HttpServletRequest request) {
        String accessToken = getAccessTokenFromCookie(request)
                .orElseThrow(UserUnauthorizedException::new);

        spotifyApi.setAccessToken(accessToken);

        return spotifyApi.getListOfCurrentUsersPlaylists()
                .build()
                .execute()
                .getItems();
    }

    private Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        } else {
            return Optional.empty();
        }

        Optional<User> userOptional = userDao.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return Optional.ofNullable(user.getAccessToken());
        } else {
            return Optional.empty();
        }
    }

    public List<PlaylistTrack> getRussianTracks(String playlistId) {
        var russianArtistsFromPlaylist = getRussianArtistsFromPlaylist(playlistId);
        return Arrays.stream(getPlaylistTracks(playlistId))
                .filter(track -> trackIncludesRussianArtists(track.getTrack().getId(), russianArtistsFromPlaylist))
                .toList();
    }

    public void removeAllRussianTracksFromPlaylist(String playlistId) {
        var json = getRussianTracks(playlistId).stream()
                .map(PlaylistTrack::getTrack)
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
    private List<String> getRussianArtistsFromPlaylist(String playlistId){
        var list = Arrays.stream(spotifyApi.getPlaylist(playlistId)
                .build()
                .execute()
                .getTracks()
                .getItems())
                .map(PlaylistTrack::getTrack)
                .map(IPlaylistItem::getId)
                .toList();

        var uniqueArtistsIdsFromPlaylist = new HashSet<String>();

        for (String trackId : list) {
            var artists = Arrays.stream(spotifyApi.getTrack(trackId)
                    .build()
                    .execute()
                    .getArtists())
                    .map(ArtistSimplified::getId)
                    .toList();

            uniqueArtistsIdsFromPlaylist.addAll(artists);
        }

        return artistDao.findAllByIds(uniqueArtistsIdsFromPlaylist).stream()
                .filter(x -> x.getCountry().equals("Russia"))
                .map(SpotifyArtist::getId)
                .toList();
    }

    @SneakyThrows
    private boolean trackIncludesRussianArtists(String trackId, List<String> ruArtists) {
        var artists = spotifyApi.getTrack(trackId)
                .build()
                .execute()
                .getArtists();

        var artistsIds = Arrays.stream(artists)
                .map(ArtistSimplified::getId)
                .toList();

        return artistsIds.stream()
                .anyMatch(ruArtists::contains);
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