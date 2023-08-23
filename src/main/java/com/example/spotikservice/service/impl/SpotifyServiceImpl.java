package com.example.spotikservice.service.impl;

import com.example.spotikservice.constants.CacheConstants;
import com.example.spotikservice.dao.CountryDao;
import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.Country;
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
import se.michaelthelin.spotify.model_objects.specification.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {
    private final SpotifyApi spotifyApi;
    private final SpotifyArtistDao artistDao;
    private final CountryDao countryDao;

    @Override
    @SneakyThrows
    @Cacheable(value = CacheConstants.REQUEST_CACHE)
    public List<PlaylistSimplified> getPlaylists() {
        return Arrays.stream(spotifyApi.getListOfCurrentUsersPlaylists()
                        .build()
                        .execute()
                        .getItems())
                .toList();
    }

    @Override
    @SneakyThrows
    public List<Album> getAlbums() {
        return Arrays.stream(spotifyApi.getCurrentUsersSavedAlbums().build()
                        .execute().getItems())
                .map(SavedAlbum::getAlbum)
                .toList();
    }

    @SneakyThrows
    @Override
    public List<Track> getSavedSongs() {
        return Arrays.stream(spotifyApi.getUsersSavedTracks()
                        .build()
                        .execute()
                        .getItems())
                .map(SavedTrack::getTrack)
                .toList();
    }


    public List<PlaylistTrack> getTracksFromPlaylistByCountry(String playlistId, String code) {
        Country country = countryDao.findCountry(code);
        var trackList = Arrays.stream(getPlaylistTracks(playlistId))
                .map(PlaylistTrack::getTrack)
                .map(IPlaylistItem::getId)
                .toList();
        var artists = getArtistsFromTrackIdAndCountry(trackList, country.getCountry());
        var tracks = getTrackIdsByCountry(trackList, country.getCode());
        return Arrays.stream(getPlaylistTracks(playlistId))
                .filter(playlistPredicate(artists, tracks))
                .toList();
    }

    @SneakyThrows
    public List<TrackSimplified> getTracksFromAlbumByCountry(String albumId, String code) {
        Country country = countryDao.findCountry(code);
        var trackArr = spotifyApi.getAlbum(albumId).build().execute().getTracks().getItems();
        List<String> trackList = Arrays.stream(trackArr)
                .map(TrackSimplified::getId)
                .toList();
        var tracks = getTrackIdsByCountry(trackList, country.getCode());
        var artists = getArtistsFromTrackIdAndCountry(trackList, country.getCountry());
        return Arrays.stream(trackArr)
                .filter(albumPredicate(tracks, artists))
                .toList();
    }

    private Predicate<? super TrackSimplified> albumPredicate(List<String> tracks, List<String> artists) {
        return track -> tracks.contains(track.getId()) || trackIncludesArtists(track.getId(), artists);
    }


    @SneakyThrows
    @Override
    public List<Track> getTracksFromAccountByCountry(String code) {
        var playlistsTracks = getPlaylists().stream()
                .map(PlaylistSimplified::getId)
                .map(id -> getTracksFromPlaylistByCountry(id, code))
                .flatMap(track -> track.stream()
                        .map(PlaylistTrack::getTrack)
                        .map(IPlaylistItem::getId))
                .distinct()
                .toList();

        var albumsTracks = getAlbums().stream()
                .map(Album::getId)
                .map(albumId -> getTracksFromAlbumByCountry(albumId, code))
                .flatMap(track -> track.stream()
                        .map(TrackSimplified::getId))
                .toList();

        var savedTracks = getTrackIdsByCountry(getSavedSongs().stream()
                .map(Track::getId).toList(), code);

        HashSet<String> tracks = new HashSet<>();
        tracks.addAll(playlistsTracks);
        tracks.addAll(albumsTracks);
        tracks.addAll(savedTracks);

        var tracklist = tracks.stream().toList();
        var list = new ArrayList<Track>();

        for (int i = 0; i < tracklist.size(); i += 50) {
            var subList = tracklist.subList(i, Math.min(i + 50, tracklist.size())).toArray(String[]::new);
            list.addAll(Arrays.stream(spotifyApi.getSeveralTracks(subList)
                    .build()
                    .execute()).toList());
        }

        return list;
    }

    public void removeAllTracksFromPlaylistByCountry(String playlistId, String code) {
        var json = getTracksFromPlaylistByCountry(playlistId, code).stream()
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

    private Predicate<? super PlaylistTrack> playlistPredicate(List<String> artists, List<String> tracks) {
        return track -> tracks.contains(track.getTrack().getId()) || trackIncludesArtists(track.getTrack().getId(), artists);
    }

    /**
     * @param code UA
     *             RU
     */
    @SneakyThrows
    private List<String> getTrackIdsByCountry(List<String> trackList, String code) {
        Country country = countryDao.findCountry(code);
        var tracksList = new LinkedList<String>();

        for (int i = 0; i < trackList.size(); i += 50) {
            var subList = trackList.subList(i, Math.min(i + 50, trackList.size()));

            Track[] tracks = spotifyApi.getSeveralTracks(subList.toArray(String[]::new))
                    .build().execute();
            var ruExternalIds = Arrays.stream(tracks)
                    .map(Track::getExternalIds)
                    .map(ExternalId::getExternalIds)
                    .flatMap(x -> x.values().stream())
                    .toList();

            for (int j = 0; j < tracks.length; j++) {
                if (ruExternalIds.get(j).startsWith(country.getCode())) {
                    tracksList.add(tracks[j].getId());
                }
            }
        }
        return tracksList;
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

    /**
     * @param country Ukraine
     *                Russia
     */
    private List<String> getArtistsFromTrackIdAndCountry(List<String> trackList, String country) {
        return new ArrayList<>(artistDao.findAllByIdsAndCountry(getArtistsIdsFromTracks(trackList), country).stream()
                .map(SpotifyArtist::getId)
                .toList());
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
    private boolean trackIncludesArtists(String trackId, List<String> artistsList) {
        var artists = spotifyApi.getTrack(trackId)
                .build()
                .execute()
                .getArtists();

        var artistsIds = Arrays.stream(artists)
                .map(ArtistSimplified::getId)
                .toList();

        return artistsIds.stream()
                .anyMatch(artistsList::contains);
    }

    public TreeMap<String, List<AlbumSimplified>> getLastReleasesFromSubscribedArtists() {
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