package com.example.spotikservice.dao;

import com.example.spotikservice.entities.SpotifyArtist;

import java.util.List;
import java.util.Set;

public interface SpotifyArtistDao {
    List<SpotifyArtist> findAllByIds(Set<String> ids);
}
