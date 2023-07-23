package com.example.spotikservice.config;

import com.example.spotikservice.props.SpotifyProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final SpotifyProps spotifyProps;

    @Bean
    public SpotifyApi spotifyApi() {
        return new SpotifyApi.Builder()
                .setClientId(spotifyProps.id())
                .setClientSecret(spotifyProps.secret())
                .setRedirectUri(URI.create(spotifyProps.uri()))
                .build();
    }
}
