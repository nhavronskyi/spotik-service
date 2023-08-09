package com.example.spotikservice.dao.impl;

import com.example.spotikservice.dao.CountryDao;
import com.example.spotikservice.entities.Country;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryDaoImpl implements CountryDao {
    private final SessionFactory sessionFactory;

    @Override
    public Country findCountry(String code) {
        try (var em = sessionFactory.createEntityManager()) {
            String query = "FROM countries WHERE code like (:code)";
            var rl = em.createQuery(query, Country.class)
                    .setParameter("code", code.toUpperCase() + "%")
                    .getResultList();
            return rl.stream()
                    .findFirst()
                    .orElse(new Country());
        }
    }
}
