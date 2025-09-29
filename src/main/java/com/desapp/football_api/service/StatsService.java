package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.CurrentStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.CurrentStatsRepository;
import com.desapp.football_api.repository.HistoricalStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class StatsService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StatsService.class);

    @Autowired
    private WhoScoredService whoScoredService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CurrentStatsRepository currentStatsRepository;

    @Autowired
    private HistoricalStatsRepository historicalStatsRepository;

    public Player getOrScrape(Long playerId, StatsType type) throws IOException, InterruptedException {
        Player player = playerRepository.findById(playerId).orElse(null);

        if (player == null) {
            logger.info("Jugador {} no encontrado en DB. Se procede a scrapear datos base ({})", playerId, type);
            TablePlayerStats tps = fetchTableStats(playerId, type);
            ensureExists(tps, playerId);
            PlayerTableStat first = tps.getPlayerTableStats().getFirst();
            // Crear player con la stats solicitada y asociarla
            player = new Player((long) first.getPlayerId(), first.getName(), first.getPositions(), first.getDateOfBirth(), first.getNationality(), first.getTeamName(), tps.getPlayerTableStats(), type);
            playerRepository.save(player);
            logger.info("Jugador {} creado y persistido desde scraping con stats {}", playerId, type);
            return player;
        } else {
            logger.info("Jugador {} obtenido desde DB", playerId);
        }

        // Buscar si ya existe la fila de stats del tipo solicitado
        if (type == StatsType.Current) {
            var existing = currentStatsRepository.findByPlayerId(playerId);
            if (existing.isPresent()) {
                logger.info("Stats CURRENT de jugador {} obtenidas desde DB (sin scraping)", playerId);
                return player;
            }
            // No existe CURRENT -> scrapear y crear la fila vinculada al mismo player
            CurrentStats newStats = buildCurrentFromScrape(playerId);
            newStats.setPlayer(player);
            currentStatsRepository.save(newStats);
            player.getStats().add(newStats);
            logger.info("Se scrapearon stats CURRENT de jugador {} y se guardaron (INSERT único por tipo)", playerId);
            return player;
        } else {
            var existing = historicalStatsRepository.findByPlayerId(playerId);
            if (existing.isPresent()) {
                logger.info("Stats HISTORICAL de jugador {} obtenidas desde DB (sin scraping)", playerId);
                return player;
            }
            // No existe HISTORICAL -> scrapear y crear la fila vinculada al mismo player
            HistoricalStats newStats = buildHistoricalFromScrape(playerId);
            newStats.setPlayer(player);
            historicalStatsRepository.save(newStats);
            player.getStats().add(newStats);
            logger.info("Se scrapearon stats HISTORICAL de jugador {} y se guardaron (INSERT único por tipo)", playerId);
            return player;
        }
    }

    private CurrentStats buildCurrentFromScrape(Long id) throws IOException, InterruptedException {
        String url = whoScoredService.getCurrentPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        TablePlayerStats table = new TablePlayerStats(response);
        ensureExists(table, id);
        List<PlayerTableStat> rows = table.getPlayerTableStats();
        return new CurrentStats(rows);
    }

    private HistoricalStats buildHistoricalFromScrape(Long id) throws IOException, InterruptedException {
        String url = whoScoredService.getHistoricalPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        TablePlayerStats table = new TablePlayerStats(response);
        ensureExists(table, id);
        List<PlayerTableStat> rows = table.getPlayerTableStats();
        return new HistoricalStats(rows);
    }

    private TablePlayerStats fetchTableStats(Long id, StatsType type) throws IOException, InterruptedException {
        String url = (type == StatsType.Current) ? whoScoredService.getCurrentPlayerLink(id) : whoScoredService.getHistoricalPlayerLink(id);
        String response = whoScoredService.fetchJSONString(url);
        return new TablePlayerStats(response);
    }

    private void ensureExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }

    private void copyStats(Stats src, Stats dst) {
        dst.setGames(src.getGames());
        dst.setMins(src.getMins());
        dst.setGoals(src.getGoals());
        dst.setAssists(src.getAssists());
        dst.setYellowCards(src.getYellowCards());
        dst.setRedCards(src.getRedCards());
        dst.setShotsPerGame(src.getShotsPerGame());
        dst.setPassSuccess(src.getPassSuccess());
        dst.setAerialsWonPerGame(src.getAerialsWonPerGame());
        dst.setRating(src.getRating());
    }
}
