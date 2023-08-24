package com.example.spotikservice.service.impl;

import com.example.spotikservice.service.RabbitMqProducer;
import com.example.spotikservice.service.SpotifyService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.*;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitMqProducerImpl implements RabbitMqProducer {
    private final RabbitTemplate template;
    private final Binding binding;
    private final SpotifyService service;

    @SneakyThrows
    public void sendSongs() {
        var artistSongs = service.getLastReleasesFromSubscribedArtists()
                .entrySet()
                .stream()
                .map(x -> new ArtistSongs(x.getKey(), x.getValue().stream()
                        .map(AlbumSimplified::getName)
                        .toList()))
                .toList();
        var idArtistSongs = new EmailArtistSongs(service.getAccountEmail(), artistSongs);
        var json = new JsonMapper().writeValueAsString(idArtistSongs);
        template.convertAndSend(binding.getExchange(), binding.getRoutingKey(), json);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class EmailArtistSongs {
        private final String email;
        private final List<ArtistSongs> artists;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class ArtistSongs {
        private final String artist;
        private final List<String> songs;
    }
}
