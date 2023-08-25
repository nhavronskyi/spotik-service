package com.example.spotikservice.dao.impl;

import com.example.spotikservice.dao.TelegramUserDao;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramUserDaoImpl implements TelegramUserDao {
    private final SessionFactory sessionFactory;

    @Override
    public List<String> getAllUsersEmailWithActiveStatus() {
        try (var em = sessionFactory.createEntityManager()) {
            return em.createQuery("SELECT email FROM telegram_users WHERE active = true", String.class)
                    .getResultList();
        }
    }
}
