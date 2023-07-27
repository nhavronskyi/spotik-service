package com.example.spotikservice.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
public record HibernateProps(String url, String user, String password, String driver, String dialect) {

}
