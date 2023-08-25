package com.example.spotikservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "telegram_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUser {
    @Id
    private String id;
    private String email;
    private Boolean active;
    private Integer checkNumber;
}
