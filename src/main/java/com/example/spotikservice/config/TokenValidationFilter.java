package com.example.spotikservice.config;

import com.example.spotikservice.dao.UserDao;
import com.example.spotikservice.entities.User;
import com.example.spotikservice.exception.UserUnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenValidationFilter {

    private final SpotifyApi spotifyApi;
    private final UserDao userDao;

    @Before("within(com.example.spotikservice.controller.SpotifyController)")
    public void validateToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

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
