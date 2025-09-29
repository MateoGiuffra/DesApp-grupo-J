package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import com.desapp.football_api.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private WhoScoredService whoScoredService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StatsService statsService;

    public Player scrapPlayerWithName(String name) throws IOException, InterruptedException {
        String playerId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new PlayerNotFoundException(name);
        });
        return statsService.getOrScrape(Long.valueOf(playerId), StatsType.Current);
    }

    public Player getOrScrape(Long id, StatsType type) throws IOException, InterruptedException {
        return playerRepository.findById(id)
                .filter(p -> p.getStats() != null && matchesType(p, type))
                .orElseGet(() -> {
                    try {
                        Player p = scrapePlayerWithIdAndType(id, type);
                        return playerRepository.save(p);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private boolean matchesType(Player p, StatsType type) {
        String discriminator = p.getStats().getClass().getSimpleName().toUpperCase();
        return (type == StatsType.Current && discriminator.contains("CURRENT"))
                || (type == StatsType.Historical && discriminator.contains("HISTORICAL"));
    }

    public Player scrapePlayerWithIdAndType(Long id, StatsType type) throws IOException, InterruptedException {
        String url = type == StatsType.Current ? whoScoredService.getCurrentPlayerLink(id) : whoScoredService.getHistoricalPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        return createPlayerFromJSON(response, id, type);
    }

    public Player createPlayerFromJSON(String response, Long id, StatsType type) {
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);

        List<PlayerTableStat> playerTableStats = tablePlayerStats.getPlayerTableStats();
        PlayerTableStat first = playerTableStats.getFirst();

        String fullname = first.getName();
        String dateOfBirth = first.getDateOfBirth();
        String nationality = first.getNationality();
        String positions = first.getPositions();
        String team = first.getTeamName();
        return new Player(id, fullname, positions, dateOfBirth, nationality, team, playerTableStats, type);
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }
}