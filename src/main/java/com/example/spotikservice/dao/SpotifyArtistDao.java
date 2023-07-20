package com.example.spotikservice.dao;

import com.example.spotikservice.entities.SpotifyArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpotifyArtistDao extends JpaRepository<SpotifyArtist, String> {
    @Override
    @NonNull
    Optional<SpotifyArtist> findById(String id);
}
