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
    public List<SpotifyArtist> findAllByIdsAndCountry(Set<String> ids, String country) {
        try (var em = sessionFactory.createEntityManager()) {
            String query = "FROM spotify_artist WHERE country = (country) AND id IN (:ids)";
            return em.createQuery(query, SpotifyArtist.class)
                    .setParameter("country", country)
                    .setParameter("ids", ids)
                    .getResultList();
        }
    }
}