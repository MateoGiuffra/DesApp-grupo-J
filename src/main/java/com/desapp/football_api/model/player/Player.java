package com.desapp.football_api.model.player;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Player entity with OneToOne to Stats.
 * <p>
 * Expected Hibernate DDL (may vary by dialect):
 * <p>
 * create table player (
 * id bigint not null,
 * date_of_birth varchar(255),
 * fullname varchar(255),
 * nationality varchar(255),
 * positions varchar(255),
 * stats_id bigint,
 * primary key (id)
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

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "stats_id")
    @JsonManagedReference
    @ToString.Exclude
    @JsonUnwrapped // <-- mismo nivel en json
    private Stats stats;

    @ManyToOne(fetch = FetchType.EAGER, optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonBackReference
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    public Player(Map<String, Object> playerMap) {
        this.fullname = playerMap.containsKey("name") ? (String) playerMap.get("name") : null;
        this.positions = playerMap.containsKey("position") ? (String) playerMap.get("position") : null;
        this.dateOfBirth = playerMap.containsKey("dateOfBirth") ? (String) playerMap.get("dateOfBirth") : null;
        this.nationality = playerMap.containsKey("nationality") ? (String) playerMap.get("nationality") : null;
    }

    public Player(Long id, String name, String positions, String dateOfBirth, String nationality, List<PlayerTableStat> playerTableStats, StatsType statsType, Team team) {
        this.id = id;
        this.fullname = name;
        this.positions = positions;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.stats = statsType.newInstance(playerTableStats);
        this.stats.setPlayer(this);
        this.team = team;
    }

    public Player(PlayerTableStat playerTableStat) {
        this.id = (long) playerTableStat.getPlayerId();
        this.fullname = playerTableStat.getName();
        this.positions = playerTableStat.getPositions();
        this.dateOfBirth = playerTableStat.getDateOfBirth();
        this.nationality = playerTableStat.getNationality();
    }

    @JsonIgnore
    public Integer getAssists() {
        return stats.getAssists();
    }

    @JsonIgnore
    public Integer getGoals() {
        return stats.getGoals();
    }

    @JsonIgnore
    public Double getRating() {
        return stats.getRating();
    }

    @JsonIgnore
    public Integer getGames() {return stats.getGames();}

    @JsonProperty("teamId")
    public Long getTeamId() {return team != null ? team.getId() : null;}
}
