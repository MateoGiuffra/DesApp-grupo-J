package com.desapp.football_api.model.match;

import com.desapp.football_api.model.Team;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@Entity
@Table(name = "match_fixture")
public class Match {
    @Id
    private Long id; // Match ID from WhoScored (index 0)

    // Store date as LocalDate for simple comparisons
    private LocalDate date; // index 2 (parsed from dd-MM-yy or dd-MM-yyyy)
    private String time; // index 3 (e.g., 18:00)

    private Long homeTeamId; // index 4
    private String homeTeamName; // index 5

    private Long awayTeamId; // index 7
    private String awayTeamName; // index 8

    private String competition; // index 16

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonBackReference
    @Schema(hidden = true)
    private Team team; // owner team for which we store this match

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

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        String d = s.trim();
        try {
            return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yy"));
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String awayNameOrTrim(String name) {
        return name == null ? null : name.trim();
    }
}
