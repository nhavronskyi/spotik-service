package com.example.spotikservice.dao.impl;

import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.SpotifyArtist;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpotifyArtistDaoImpl implements SpotifyArtistDao {
    private final SessionFactory sessionFactory;

    @Override
    public Optional<SpotifyArtist> findById(String id) {
        try (var em = sessionFactory.createEntityManager()){
            var spotifyArtist = em.find(SpotifyArtist.class, id);
            return Optional.ofNullable(spotifyArtist);
        }
    }
}
