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
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "team", indexes = {@Index(name = "idx_team_name", columnList = "name")})
public class Team {
    private static final Logger logger = LoggerFactory.getLogger(Team.class);
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
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER,
            orphanRemoval = false
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
    private LocalDate lastTimeScrapped;

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
            logger.info("Team constructor error: {}", e.getMessage());
            throw new WhoScoredServiceUnavailableException();
        }
    }

    public Team(Long teamId, String teamName, TeamStats teamStats, List<Player> squadList, List<Match> matches) {
        this.id = teamId;
        this.name = teamName;
        this.stats = teamStats;
        this.squadList = squadList;
        if (squadList != null) {
            squadList.forEach(p -> p.setTeam(this));
        }
        this.matches = matches;
    }

    public ObjectNode getAdvancedStats() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        List<Match> pastMatches = getPastMatches();

        if (stats == null || squadList.isEmpty() || pastMatches.isEmpty()) {
            return root;
        }

        root.put("goalsPerGame", (double) stats.getGoals() / stats.getGames());

        int goalsConceded = 0;
        int cleanSheets = 0;
        for (Match match : pastMatches) {
            if (match.getHomeTeamId().equals(id)) {
                goalsConceded += match.getAwayGoals();
                if (match.getAwayGoals() == 0) {
                    cleanSheets++;
                }
            } else {
                goalsConceded += match.getHomeGoals();
                if (match.getHomeGoals() == 0) {
                    cleanSheets++;
                }
            }
        }
        root.put("goalsConceded", goalsConceded);
        root.put("goalsConcededPerGame", (double) goalsConceded / stats.getGames());
        root.put("goalDifference", stats.getGoals() - goalsConceded);
        root.put("cleanSheets", cleanSheets);

        StringBuilder recentForm = new StringBuilder();
        pastMatches.stream()
                .sorted(Comparator.comparing(Match::getDate).reversed())
                .limit(5)
                .forEach(match -> {
                    if (match.getHomeTeamId().equals(id)) {
                        if (match.getHomeGoals() > match.getAwayGoals()) {
                            recentForm.append("W");
                        } else if (match.getHomeGoals() < match.getAwayGoals()) {
                            recentForm.append("L");
                        } else {
                            recentForm.append("D");
                        }
                    } else {
                        if (match.getAwayGoals() > match.getHomeGoals()) {
                            recentForm.append("W");
                        } else if (match.getAwayGoals() < match.getHomeGoals()) {
                            recentForm.append("L");
                        } else {
                            recentForm.append("D");
                        }
                    }
                });
        root.put("recentForm", recentForm.toString());

        Optional<Player> topScorer = squadList.stream().max(Comparator.comparing(Player::getGoals));
        topScorer.ifPresent(player -> {
            ObjectNode topScorerNode = mapper.createObjectNode();
            topScorerNode.put("fullname", player.getFullname());
            topScorerNode.put("goals", player.getGoals());
            root.set("topScorer", topScorerNode);
        });

        Optional<Player> topAssister = squadList.stream().max(Comparator.comparing(Player::getAssists));
        topAssister.ifPresent(player -> {
            ObjectNode topAssisterNode = mapper.createObjectNode();
            topAssisterNode.put("fullname", player.getFullname());
            topAssisterNode.put("assists", player.getAssists());
            root.set("topAssister", topAssisterNode);
        });

        return root;
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

    // Aggregate update helpers
    public void applyPlayers(List<Player> players) {
        this.getSquadList().clear();
        if (players != null) {
            players.forEach(this::addPlayer);
        }
    }

    public void applyStats(TeamStats teamStats) {
        if (teamStats != null) {
            teamStats.setTeam(this);
        }
        this.setStats(teamStats);
    }

    public void applyMatches(List<Match> matches) {
        if (matches == null) {
            this.setMatches(new ArrayList<>());
            return;
        }
        matches.forEach(m -> m.setTeam(this));
        this.setMatches(matches);
    }

    public Boolean hasToBeScrapped() {
        int maxHoursToScrap = 4;
        return this.lastTimeScrapped == null || Duration.between(this.lastTimeScrapped.atStartOfDay(), LocalDate.now().atStartOfDay()).toHours() > maxHoursToScrap;
    }
}
