package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import com.desapp.football_api.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class PlayerService {

    @Autowired
    private WhoScoredService whoScoredService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StatsService statsService;

    public Player scrapPlayerWithName(String name, StatsType statsType) throws IOException, InterruptedException {
        String playerId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new PlayerNotFoundException(name);
        });
        return this.getPlayerByIdAndType(Long.valueOf(playerId), statsType);
    }

    public Player getPlayerByIdAndType(Long id, StatsType type) throws IOException, InterruptedException {
        Player player = getPlayerWithStatsById(id, type);
        if (player == null || player.getStats() == null) {
            player = scrapePlayerWithIdAndType(id, type);
        }
        return player;
    }

    public Player getPlayerWithStatsById(Long id, StatsType type) {
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null) {
            return null;
        }
        Stats stats = statsService.getStatsByPlayerId(id, type);
        player.setStats(stats);
        return player;
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
        Player player = new Player(id, fullname, positions, dateOfBirth, nationality, team, playerTableStats, type);

        return playerRepository.save(player);
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
    }

}