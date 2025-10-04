package com.desapp.football_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "match_fixture")
public class Match {
    @Id
    private Long id; // Match ID from WhoScored (index 0)

    private String date; // index 2 (e.g., 18-06-25)
    private String time; // index 3 (e.g., 18: 00)

    private Long homeTeamId; // index 4
    private String homeTeamName; // index 5

    private Long awayTeamId; // index 7
    private String awayTeamName; // index 8

    private String competition; // index 16

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonBackReference
    private Team team; // owner team for which we store this match
}
