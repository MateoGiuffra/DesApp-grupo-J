package com.desapp.football_api.model;

import com.desapp.football_api.exceptions.who_scored.WhoScoredServiceUnavailableException;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "team")
public class Team {
    @Id
    @EqualsAndHashCode.Include
    private Long id;
    private String name;


    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "stats_id")
    @JsonManagedReference
    @ToString.Exclude
    @JsonUnwrapped
    private TeamStats stats;

    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<Player> squadList = new ArrayList<>();

    @OneToMany(
            mappedBy = "team",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    @ToString.Exclude
    private List<Match> matches = new ArrayList<>();

    // Convenience constructor to build a team with players and keep both sides in sync
    public Team(Long id, String name, List<Player> players) {
        this.id = id;
        this.name = name;
        this.squadList = new ArrayList<>();
        if (players != null) {
            players.forEach(this::addPlayer);
        }
    }

    public Team(Long id, String body) {
        this.id = id;
        this.squadList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(body);
            List<TableStat> tableStatList = mapper.readerForListOf(TableStat.class).readValue(root.get("playerTableStats").toString());
            tableStatList.forEach((playerTableStat -> {
                Player player = new Player(playerTableStat);
                this.addPlayer(player);
            }));
        } catch (Exception e) {
            System.out.println("Team constructor error:   " + e.getMessage());
            throw new WhoScoredServiceUnavailableException();
        }
    }

    public void addPlayer(Player p) {
        if (p == null) return;
        p.setTeam(this);
        if (!squadList.contains(p)) {
            this.squadList.add(p);
        }
    }

    public void removePlayer(Player p) {
        if (p == null) return;
        this.squadList.remove(p);
        if (p.getTeam() == this) {
            p.setTeam(null);
        }
    }

    public List<Match> getPastMatches() {
        LocalDate today = LocalDate.now();
        return this.matches.stream()
                .filter(m -> m.getDate() != null && m.getDate().isBefore(today))
                .toList();
    }

    public List<Match> getUpcomingMatches() {
        LocalDate today = LocalDate.now();
        return this.matches.stream()
                .filter(m -> m.getDate() != null && m.getDate().isAfter(today))
                .toList();
    }

    public List<Match> getAwayMatches() {
        return this.matches.stream()
                .filter(m -> m.getAwayTeamId().equals(this.id))
                .toList();
    }

    public List<Match> getHomeMatches() {
        return this.matches.stream()
                .filter(m -> m.getHomeTeamId().equals(this.id))
                .toList();
    }

    public List<Match> getFilterMatches(MatchType matchType, MatchLocation matchLocation) {
        List<Match> intersection = new ArrayList<>(matchType.filter(this));
        intersection.retainAll(matchLocation.filter(this));
        return intersection;

    }
}


