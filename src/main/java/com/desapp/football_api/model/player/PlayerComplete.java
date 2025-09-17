package com.desapp.football_api.model.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerComplete extends Player{
    private String team;
    private int games;
    private int mins;
    private int goals;
    private  int assists;
    private int yellowCards;
    private int redCards;
    private int shotsPerGame;
    private double passSuccess;
    private double aerialsWonPerGame;
    private double rating;

}
