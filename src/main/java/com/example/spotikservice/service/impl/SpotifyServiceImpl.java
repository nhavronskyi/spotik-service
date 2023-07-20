package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.SpotifyArtist;
import com.example.spotikservice.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    @SneakyThrows
    public List<PlaylistTrack> getRussianTracks(String id) {
        var tracks = spotifyApi.getPlaylist(id)
                .build()
                .execute()
                .getTracks()
                .getItems();
        return Arrays.stream(tracks)
                .filter(track -> isRussianArtist(track.getTrack().getId()))
                .toList();
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
}
