package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.model.table_stats.TablePlayerStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PlayerService {

    @Autowired
    private WhoScoredService whoScoredService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StatsService statsService;

    @Autowired
    private TeamRepository teamRepository;

    public Player getPlayerByIdAndType(Long id, StatsType type) throws IOException, InterruptedException {
        return ScrapeHelper.getOrScrape(() -> getPlayerWithStatsByIdAndType(id, type), this::hasToScrap, () -> scrapePlayerWithIdAndType(id, type));
    }

    public Player getPlayerByNameAndType(String name, StatsType type) throws IOException, InterruptedException {
        return ScrapeHelper.getOrScrape(() -> getPlayerByName(name), this::hasToScrap, () -> scrapePlayerWithName(name, type));
    }

    public Player getPlayerByName(String name) {
        String normalizedName = normalizeName(name);
        return playerRepository.findByFullname(normalizedName).orElse(null);
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    private Player getPlayerWithStatsByIdAndType(Long id, StatsType type) {
        Optional<Player> maybePlayer = playerRepository.findById(id);
        return maybePlayer.map(player -> {
            PlayerStats stats = statsService.getStatsByPlayerId(player.getId(), type);
            player.setStats(stats);
            return player;
        }).orElse(null);
    }

    private Player scrapePlayerWithName(String name, StatsType statsType) {
        String playerId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new PlayerNotFoundException(name);
        });
        return scrapePlayerWithIdAndType(Long.valueOf(playerId), statsType);
    }

    public Player scrapePlayerWithIdAndType(Long id, StatsType type) {
        TablePlayerStats tablePlayerStats = getTableStat(type, id);
        TableStat first = tablePlayerStats.getTableStats().getFirst();

        Long teamId = (long) first.getTeamId();
        Team team = new Team(teamId, first.getTeamName(), null, List.of(), List.of());
        teamRepository.save(team);

        Player player = createPlayer(id, type, team);
        return playerRepository.save(player);
    }


    public Player createPlayer(Long id, StatsType type, Team team) {
        TablePlayerStats tablePlayerStats = getTableStat(type, id);
        TableStat first = tablePlayerStats.getTableStats().getFirst();

        return new Player(
                id,
                first.getName(),
                first.getPositions(),
                first.getDateOfBirth(),
                first.getNationality(),
                tablePlayerStats.getTableStats(),
                type,
                team
        );
    }

    private TablePlayerStats getTableStat(StatsType type, Long id) {
        String url = type.newInstance().getPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);
        return tablePlayerStats;
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }

    private Boolean hasToScrap(Player player) {
        return player == null || player.getStats() == null;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return name;

        return Arrays.stream(name.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }
}
