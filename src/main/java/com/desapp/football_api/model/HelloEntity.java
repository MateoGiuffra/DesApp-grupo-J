package com.desapp.football_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class HelloEntity {
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;
    private String hello = "HOLA UNQ!";

    public HelloEntity(String body) {
        this.hello = body;
    }
}
