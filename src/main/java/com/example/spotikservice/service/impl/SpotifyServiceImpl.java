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
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
            long accessTokenExpirationTime = Instant.now().getEpochSecond() + execute.getExpiresIn();
            spotifyApi.setAccessToken(accessToken);

            String userId = spotifyApi.getCurrentUsersProfile().build().execute().getId();
            User user = new User(userId, accessToken, refreshToken, accessTokenExpirationTime);
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
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getCookies)
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> "user_id".equals(cookie.getName()))
                        .findFirst()
                        .map(Cookie::getValue))
                .flatMap(userDao::findById)
                .map(this::getRefreshedAccessToken);
    }

    private String getRefreshedAccessToken(User u) {
        if (Instant.now().getEpochSecond() < u.getAccessTokenExpirationTime()) {
            return u.getAccessToken();
        } else {
            try {
                AuthorizationCodeCredentials execute = spotifyApi.authorizationCodeRefresh()
                        .refresh_token(u.getRefreshToken())
                        .build()
                        .execute();

                String newAccessToken = execute.getAccessToken();
                long newAccessTokenExpirationTime = Instant.now().getEpochSecond() + execute.getExpiresIn();
                u.setAccessToken(newAccessToken);
                u.setAccessTokenExpirationTime(newAccessTokenExpirationTime);
                userDao.save(u);

                return newAccessToken;
            } catch (IOException | ParseException | SpotifyWebApiException e) {
                return null;
            }
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

    private List<String> getRussianArtistsFromPlaylist(String playlistId) {
        var trackList = Arrays.stream(getPlaylistTracks(playlistId))
                .map(PlaylistTrack::getTrack)
                .map(IPlaylistItem::getId)
                .toList();

        return artistDao.findAllByIdsAndCountry(getArtistsIdsFromTracks(trackList), "Russia").stream()
                .map(SpotifyArtist::getId)
                .toList();
    }

    @SneakyThrows
    private Set<String> getArtistsIdsFromTracks(List<String> trackIds) {
        var artistList = new HashSet<String>();

        for (int i = 0; i < trackIds.size(); i += 50) {
            var subList = trackIds.subList(i, Math.min(i + 50, trackIds.size()));

            var tracks = spotifyApi.getSeveralTracks(subList.toArray(String[]::new))
                    .build()
                    .execute();
            Set<String> artists = Arrays.stream(tracks)
                    .map(Track::getArtists)
                    .flatMap(Arrays::stream)
                    .map(ArtistSimplified::getId)
                    .collect(Collectors.toSet());
            artistList.addAll(artists);
        }
        return artistList;
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
    private Map<String, List<AlbumSimplified>> getAllNewReleases(List<Artist> artists) {
        var map = new HashMap<String, List<AlbumSimplified>>();
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