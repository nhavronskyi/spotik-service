package com.example.spotikservice.config;

import com.example.spotikservice.props.RabbitMQProps;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final RabbitMQProps props;

    @Bean
    public Queue queue() {
        return new Queue(props.queue());
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(props.exchange());
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue())
                .to(exchange())
                .with(props.routingKey());
    }
}
