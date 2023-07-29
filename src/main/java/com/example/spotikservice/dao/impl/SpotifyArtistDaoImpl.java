package com.example.spotikservice.dao.impl;

import com.example.spotikservice.dao.SpotifyArtistDao;
import com.example.spotikservice.entities.SpotifyArtist;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpotifyArtistDaoImpl implements SpotifyArtistDao {
    private final SessionFactory sessionFactory;

    @Override
    public List<SpotifyArtist> findAllByIds(Set<String> ids) {
        try (var em = sessionFactory.createEntityManager()) {
            String query = "FROM spotify_artist WHERE id IN (:ids)";
            return em.createQuery(query, SpotifyArtist.class)
                    .setParameter("ids", ids)
                    .getResultList();
        }
    }
}
