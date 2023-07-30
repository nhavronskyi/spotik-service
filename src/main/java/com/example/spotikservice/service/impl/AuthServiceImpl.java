package com.example.spotikservice.service.impl;

import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.User;
import com.example.spotikservice.exception.UserUnauthorizedException;
import com.example.spotikservice.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SpotifyApi spotifyApi;
    private final UserDao userDao;

    @Override
    public String authorize() {
        return spotifyApi
                .authorizationCodeUri()
                .scope("user-follow-read playlist-modify-public")
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
            response.addCookie(cookie);

            return HttpStatus.SC_OK;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return HttpStatus.SC_UNAUTHORIZED;
        }
    }

    public void setAccessToken(HttpServletRequest request) {
        String accessToken = getAccessTokenFromCookie(request)
                .orElseThrow(UserUnauthorizedException::new);

        spotifyApi.setAccessToken(accessToken);
    }

    private Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getCookies)
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> "user_id".equals(cookie.getName()))
                        .findFirst()
                        .map(Cookie::getValue))
                .flatMap(userDao::findById)
                .map(this::getRefreshedAccessToken);
    }

    private String getRefreshedAccessToken(User u) {
        if (Instant.now().getEpochSecond() < u.getAccessTokenExpirationTime()) {
            return u.getAccessToken();
        } else {
            try {
                AuthorizationCodeCredentials execute = spotifyApi.authorizationCodeRefresh()
                        .refresh_token(u.getRefreshToken())
                        .build()
                        .execute();

                String newAccessToken = execute.getAccessToken();
                long newAccessTokenExpirationTime = Instant.now().getEpochSecond() + execute.getExpiresIn();
                u.setAccessToken(newAccessToken);
                u.setAccessTokenExpirationTime(newAccessTokenExpirationTime);
                userDao.save(u);

                return newAccessToken;
            } catch (IOException | ParseException | SpotifyWebApiException e) {
                return null;
            }
        }
    }
}
