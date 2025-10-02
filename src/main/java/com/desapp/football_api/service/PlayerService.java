package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
        return attachStats(playerRepository.findById(id), type);
    }

    private Player attachStats(Optional<Player> maybePlayer, StatsType type) {
        return maybePlayer.map(player -> {
            Stats stats = statsService.getStatsByPlayerId(player.getId(), type);
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
        String url = type.newInstance().getPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        return createPlayerFromJSON(response, id, type);
    }

    public Player createPlayerFromJSON(String response, Long id, StatsType type) {
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);

        PlayerTableStat first = tablePlayerStats.getPlayerTableStats().getFirst();

        Long teamId = (long) first.getTeamId();
        Team team = teamRepository.findById(teamId).orElse(new Team(teamId, first.getTeamName(), null));

        Player player = new Player(
                id,
                first.getName(),
                first.getPositions(),
                first.getDateOfBirth(),
                first.getNationality(),
                tablePlayerStats.getPlayerTableStats(),
                type,
                team
        );

        try {
            return playerRepository.save(player);
        } catch (Exception e) {
            log.error(String.valueOf(e.getCause()));
        }
        return player;
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }


    private Boolean hasToScrap(Player player) {
        return player == null || player.getStats() == null;
    }

    /**
     * Normaliza un nombre a formato capitalizado:
     * "lionel messi" → "Lionel Messi"
     */
    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return name;

        return java.util.Arrays.stream(name.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }
}
