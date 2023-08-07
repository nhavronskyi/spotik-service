package com.example.spotikservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "countries")
@Setter
@Getter
public class Country {
    @Id
    private String code;
    private String country;
}
