package com.example.spotikservice.service.impl;

import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.User;
import com.example.spotikservice.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.time.Instant;

import static se.michaelthelin.spotify.enums.AuthorizationScope.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SpotifyApi spotifyApi;

    private final UserDao userDao;

    @Override
    public String authorize() {
        return spotifyApi
                .authorizationCodeUri()
                .scope(USER_LIBRARY_READ,
                        USER_LIBRARY_MODIFY,
                        USER_FOLLOW_READ,
                        USER_FOLLOW_MODIFY,
                        PLAYLIST_MODIFY_PRIVATE,
                        PLAYLIST_MODIFY_PUBLIC)
                .build()
                .execute()
                .toString();
    }

    @Override
    public int setAccessToken(String code, HttpServletResponse response) {
        try {
            var execute = spotifyApi.authorizationCode(code)
                    .build()
                    .execute();

            String accessToken = execute.getAccessToken();
            String refreshToken = execute.getRefreshToken();
            long accessTokenExpirationTime = Instant.now().getEpochSecond() + execute.getExpiresIn();
            spotifyApi.setAccessToken(accessToken);

            String userId = spotifyApi.getCurrentUsersProfile().build().execute().getId();
            User user = new User(userId, accessToken, refreshToken, accessTokenExpirationTime);
            userDao.save(user);

            Cookie cookie = new Cookie("user_id", userId);
            cookie.setPath("/"); // Параметр "Path" вказує шлях, на якому доступне кукі (у цьому випадку доступне на всій домені)
            cookie.setMaxAge(14 * 24 * 60 * 60);
            response.addCookie(cookie);

            return HttpStatus.SC_OK;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return HttpStatus.SC_UNAUTHORIZED;
        }
    }
}
