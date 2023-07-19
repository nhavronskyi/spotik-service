package com.example.spotikservice.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public record SpotifyProps(String id, String secret, String uri) {
}
