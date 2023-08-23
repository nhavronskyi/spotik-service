package com.example.spotikservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RabbitMqProducer {
    private final RabbitTemplate template;
    private final Binding binding;
    private final SpotifyService service;

    public void sendSongs() {
        template.convertAndSend(binding.getExchange(), binding.getRoutingKey(), service.getLastReleasesFromSubscribedArtists()
                .entrySet()
                .stream()
                .map(track -> track.getKey() + ": " + track.getValue().stream()
                        .map(AlbumSimplified::getName)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));
    }
}