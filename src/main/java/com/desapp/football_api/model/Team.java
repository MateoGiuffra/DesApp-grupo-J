package com.desapp.football_api.model;

import com.desapp.football_api.exceptions.who_scored.WhoScoredServiceUnavailableException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "team")
public class Team {
    @Id
    private Long id;
    private String name;
    @OneToMany(
            mappedBy = "team",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<Player> squadList = new ArrayList<>();

    @OneToMany(
            mappedBy = "team",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private List<Match> upcomingMatches = new ArrayList<>();

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
            List<PlayerTableStat> playerTableStatList = mapper.readerForListOf(PlayerTableStat.class).readValue(root.get("playerTableStats").toString());
            playerTableStatList.forEach((playerTableStat -> {
                Player player = new Player(playerTableStat);
                this.addPlayer(player);
            }));
        } catch (Exception e) {
            throw new WhoScoredServiceUnavailableException();
        }
    }

    // Helper methods to maintain bi-directional association
    public void addPlayer(Player p) {
        if (p == null) return;
        if (!this.squadList.contains(p)) {
            this.squadList.add(p);
        }
        p.setTeam(this);
    }

    public void removePlayer(Player p) {
        if (p == null) return;
        this.squadList.remove(p);
        if (p.getTeam() == this) {
            p.setTeam(null);
        }
    }
}


