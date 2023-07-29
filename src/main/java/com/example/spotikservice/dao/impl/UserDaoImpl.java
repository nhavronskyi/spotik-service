package com.example.spotikservice.dao.impl;

import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    private final SessionFactory sessionFactory;

    @Override
    public void save(User user) {
        try (var em = sessionFactory.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        }
    }

    @Override
    public Optional<User> findById(String userId) {
        try (var em = sessionFactory.createEntityManager()) {
            var spotifyArtist = em.find(User.class, userId);
            return Optional.ofNullable(spotifyArtist);
        }
    }
}
