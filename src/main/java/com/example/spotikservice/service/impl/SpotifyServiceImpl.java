package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.service.SpotifyService;
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
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
    private final SpotifyApi spotifyApi;

    @Override
    public int setAccessToken(String code) {
        try {
            var execute = spotifyApi.authorizationCode(code)
                    .build().execute();
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
                .build().execute().getItems();
    }

    @SneakyThrows
    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public TreeMap<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists() {
        var map = new TreeMap<String, List<AlbumSimplified>>();

        var artists = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
                .build()
                .execute()
                .getItems();

        for (Artist artist : artists) {
            var songs = Arrays.stream(spotifyApi.getArtistsAlbums(artist.getId())
                    .build()
                    .execute()
                    .getItems())
                    .filter(x -> {
                        try {
                            return LocalDate.parse(x.getReleaseDate()).isAfter(LocalDate.now().minusMonths(1));
                        }catch (DateTimeParseException e){
                            return false;
                        }
            }).toList();
            map.put(artist.getName(), songs);
        }
        return map;
    }
}