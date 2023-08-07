package com.example.spotikservice.dao;

import com.example.spotikservice.entities.Country;

public interface CountryDao {
    Country findCountry(String code);
}
