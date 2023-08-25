package com.example.spotikservice;

import com.example.spotikservice.props.HibernateProps;
import com.example.spotikservice.props.RabbitMQProps;
import com.example.spotikservice.props.SpotifyProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties({SpotifyProps.class, HibernateProps.class, RabbitMQProps.class})
@EnableCaching
@EnableTransactionManagement
@EnableScheduling
public class SpotikServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotikServiceApplication.class, args);
    }

}