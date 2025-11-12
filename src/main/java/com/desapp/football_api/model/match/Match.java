package com.desapp.football_api.model.match;

import com.desapp.football_api.model.team.Team;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@NoArgsConstructor
@Entity
@Table(name = "match")
public class Match {
    @Id
    private Long id;

    private LocalDate date;
    private String time;

    private Long homeTeamId;
    private String homeTeamName;
    private Integer homeGoals;

    private Long awayTeamId;
    private String awayTeamName;
    private Integer awayGoals;

    private String competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonBackReference
    @Schema(hidden = true)
    private Team team;

    public Match(Long id, String date, String time, Long homeTeamId, String homeTeamName, Long awayTeamId, String awayTeamName, String competition, Team team) {
        this.id = id;
        this.date = parseDate(date);
        this.time = time;
        this.homeTeamId = homeTeamId;
        this.homeTeamName = homeTeamName;
        this.awayTeamId = awayTeamId;
        this.awayTeamName = awayNameOrTrim(awayTeamName);
        this.competition = competition;
        this.team = team;
    }

    public Match(Long id, String date, String time, Long homeTeamId, String homeTeamName, Integer homeGoals, Long awayTeamId, String awayTeamName, Integer awayGoals, String competition, Team team) {
        this.id = id;
        this.date = parseDate(date);
        this.time = time;
        this.homeTeamId = homeTeamId;
        this.homeTeamName = homeTeamName;
        this.homeGoals = homeGoals;
        this.awayTeamId = awayTeamId;
        this.awayTeamName = awayNameOrTrim(awayTeamName);
        this.awayGoals = awayGoals;
        this.competition = competition;
        this.team = team;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        String d = s.trim();
        try {
            return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yy"));
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    private static String awayNameOrTrim(String name) {
        return name == null ? null : name.trim();
    }
}
