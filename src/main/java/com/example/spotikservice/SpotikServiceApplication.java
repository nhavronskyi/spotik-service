package com.example.spotikservice;

import com.example.spotikservice.props.SpotifyProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SpotifyProps.class)
public class SpotikServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotikServiceApplication.class, args);
	}

}