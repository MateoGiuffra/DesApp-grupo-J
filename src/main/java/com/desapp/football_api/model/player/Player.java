package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Player entity with OneToOne to Stats.
 *
 * Expected Hibernate DDL (may vary by dialect):
 *
 * create table player (
 *   id bigint not null,
 *   date_of_birth varchar(255),
 *   fullname varchar(255),
 *   nationality varchar(255),
 *   positions varchar(255),
 *   stats_id bigint,
 *   primary key (id)
 * );
 * alter table if exists player add constraint FK_player_stats foreign key (stats_id) references stats;
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "player")
public class Player {
    @Id
    private Long id; // we use external id as primary key

    private String fullname;
    private String positions;
    private String dateOfBirth;
    private String nationality;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private java.util.List<com.desapp.football_api.model.stats.Stats> stats = new java.util.ArrayList<>();

    public Player(Map<String, Object> playerMap) {
        this.fullname = playerMap.containsKey("name") ? (String) playerMap.get("name") : null;
        this.positions = playerMap.containsKey("position") ? (String) playerMap.get("position") : null;
        this.dateOfBirth = playerMap.containsKey("dateOfBirth") ? (String) playerMap.get("dateOfBirth") : null;
        this.nationality = playerMap.containsKey("nationality") ? (String) playerMap.get("nationality") : null;
    }

    public Player(Long id, String name, String positions, String dateOfBirth, String nationality, String team, List<PlayerTableStat> playerTableStats, StatsType statsType) {
        this.id = id;
        this.fullname = name;
        this.positions = positions;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        com.desapp.football_api.model.stats.Stats s =
                (statsType == StatsType.Current)
                        ? new com.desapp.football_api.model.stats.CurrentStats(playerTableStats)
                        : new com.desapp.football_api.model.stats.HistoricalStats(playerTableStats);
        s.setPlayer(this);
        this.stats.add(s);
    }

    public Player(PlayerTableStat playerTableStat) {
        this.id = (long) playerTableStat.getPlayerId();
        this.fullname = playerTableStat.getName();
        this.positions = playerTableStat.getPositions();
        this.dateOfBirth = playerTableStat.getDateOfBirth();
        this.nationality = playerTableStat.getNationality();
    }

    private com.desapp.football_api.model.stats.Stats preferredStats() {
        if (this.stats == null || this.stats.isEmpty()) return null;
        // Prefer CURRENT if present
        for (com.desapp.football_api.model.stats.Stats s : this.stats) {
            if (s instanceof com.desapp.football_api.model.stats.CurrentStats) return s;
        }
        return this.stats.get(0);
    }

    public Integer getAssists() {
        com.desapp.football_api.model.stats.Stats s = preferredStats();
        return s != null ? s.getAssists() : null;
    }

    public Integer getGoals() {
        com.desapp.football_api.model.stats.Stats s = preferredStats();
        return s != null ? s.getGoals() : null;
    }

    public Double getRating() {
        com.desapp.football_api.model.stats.Stats s = preferredStats();
        return s != null ? s.getRating() : null;
    }

    public Integer getGames() {
        com.desapp.football_api.model.stats.Stats s = preferredStats();
        return s != null ? s.getGames() : null;
    }
}
