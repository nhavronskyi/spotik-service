package com.example.spotikservice.service.impl;

import com.example.spotikservice.dao.TelegramUserDao;
import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.User;
import com.example.spotikservice.service.RabbitMqProducer;
import com.example.spotikservice.service.SpotifyService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.*;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitMqProducerImpl implements RabbitMqProducer {
    private final RabbitTemplate template;
    private final Binding binding;
    private final SpotifyService service;
    private final TelegramUserDao telegramUserDao;
    private final UserDao userDao;
    private final SpotifyApi spotifyApi;

    @SneakyThrows
    @Scheduled(cron = "0 0 7 * * *")
    public void sendSongs() {
        var users = userDao.getAllUsersByEmails(telegramUserDao.getAllUsersEmailWithActiveStatus());
        List<EmailArtistSongs> list = new LinkedList<>();
        for (User user : users) {
            spotifyApi.setAccessToken(user.getAccessToken());
            var artistSongs = service.getLastReleasesFromSubscribedArtists()
                    .entrySet()
                    .stream()
                    .map(x -> new ArtistSongs(x.getKey(), x.getValue().stream()
                            .map(AlbumSimplified::getName)
                            .toList()))
                    .toList();
            var emailArtistSongs = new EmailArtistSongs(service.getAccountEmail(), artistSongs);
            list.add(emailArtistSongs);
        }
        var json = new JsonMapper().writeValueAsString(list);
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
