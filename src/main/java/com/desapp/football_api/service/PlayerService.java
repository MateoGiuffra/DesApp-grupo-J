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
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class PlayerService {

    @Autowired
    private WhoScoredService whoScoredService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StatsService statsService;


    public Player getPlayerByIdAndType(Long id, StatsType type) throws IOException, InterruptedException {
        return getOrScrapePlayer(() -> getPlayerWithStatsById(id, type), () -> scrapePlayerWithIdAndType(id, type));
    }

    public Player getPlayerByNameAndType(String name, StatsType type) throws IOException, InterruptedException {
        return getOrScrapePlayer(() -> getPlayerByName(name), () -> scrapePlayerWithName(name, type));
    }

    public Player getPlayerByName(String name) {
        String normalizedName = normalizeName(name);
        return playerRepository.findByFullname(normalizedName).orElse(null);
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    private Player getPlayerWithStatsById(Long id, StatsType type) {
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

    private Player scrapePlayerWithIdAndType(Long id, StatsType type) {
        String url = type.newInstance().getPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        return createPlayerFromJSON(response, id, type);
    }

    public Player createPlayerFromJSON(String response, Long id, StatsType type) {
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);

        PlayerTableStat first = tablePlayerStats.getPlayerTableStats().getFirst();
        Player player = new Player(
                id,
                first.getName(),
                first.getPositions(),
                first.getDateOfBirth(),
                first.getNationality(),
                first.getTeamName(),
                tablePlayerStats.getPlayerTableStats(),
                type
        );

        return playerRepository.save(player);
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }

    /**
     * Lógica común: intenta obtener un jugador desde repositorio/BD,
     * si no existe o le faltan stats, lo obtiene vía scraping.
     */
    private Player getOrScrapePlayer(Supplier<Player> fromDb, Supplier<Player> fromScraping) throws IOException, InterruptedException {
        Player player = fromDb.get();
        if (player == null || player.getStats() == null) {
            player = fromScraping.get();
        }
        return player;
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
