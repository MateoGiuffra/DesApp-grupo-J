package com.desapp.football_api.model.table_player_stats;

import com.desapp.football_api.utils.WhoScoredHelper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerTableStat {
    private int height;
    private int weight;
    private int age;
    private boolean isManOfTheMatch;
    private boolean isActive;
    private String playedPositions;
    private String playedPositionsShort;
    private String teamRegionName;
    private String regionCode;
    private String tournamentShortName;
    private int apps;
    private int subOn;
    private int manOfTheMatch;
    private int goal;
    private int assistTotal;
    private double shotsPerGame;
    private double aerialWonPerGame;
    private String name;
    private String firstName;
    private String lastName;
    private int playerId;
    private String positionText;
    private int teamId;
    private String teamName;
    private int seasonId;
    private String seasonName;
    private boolean isOpta;
    private int tournamentId;
    private int tournamentRegionId;
    private String tournamentRegionCode;
    private String tournamentRegionName;
    private String tournamentName;
    private double rating;
    private int minsPlayed;
    private double yellowCard;
    private double redCard;
    private double passSuccess;
    private int ranking;

    public String getNationality() {
        return WhoScoredHelper.getCountryNameFromCode(this.regionCode);
    }

    public String getDateOfBirth() {
        return WhoScoredHelper.calculateBirthDateByAge(this.age);
    }

    public String getPositions() {
        return WhoScoredHelper.parsePlayedPositions(this.playedPositions);
    }
}
