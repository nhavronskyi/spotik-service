package com.example.spotikservice.dao;

import com.example.spotikservice.entities.SpotifyArtist;

import java.util.Optional;

public interface SpotifyArtistDao {
    Optional<SpotifyArtist> findById(String id);
}
