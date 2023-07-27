package com.example.spotikservice.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "spotify_artist")
@Getter
@Setter
public class SpotifyArtist {
    @Id
    private String id;
    private String country;
    private String name;
}
