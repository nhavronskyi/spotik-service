package com.example.spotikservice.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitMQProps(String queue, String exchange, String routingKey) {
}
