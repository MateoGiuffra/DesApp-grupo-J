package com.desapp.football_api.services.impl;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.model.table_stats.TablePlayerStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.services.PlayerService;
import com.desapp.football_api.utils.ScrapeHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.desapp.football_api.utils.Normalizer.normalizeName;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final WhoScoredServiceImpl whoScoredServiceImpl;
    private final PlayerRepository playerRepository;
    private final StatsServiceImpl statsServiceImpl;
    private final TeamRepository teamRepository;

    @Override
    public Player getPlayerByIdAndType(Long id, StatsType type) {
        return ScrapeHelper.getOrScrape(() -> getPlayerWithStatsByIdAndType(id, type),
                player -> hasToScrap(player, type), () -> scrapePlayerWithIdAndType(id, type));
    }

    @Override
    public Player getPlayerByNameAndType(String name, StatsType type) {
        return ScrapeHelper.getOrScrape(() -> getPlayerWithStatsByNameAndType(name, type),
                player -> hasToScrap(player, type), () -> scrapePlayerWithName(name, type));
    }

    @Override
    public Player getPlayerByName(String name) {
        String normalizedName = normalizeName(name);
        return playerRepository.findByFullname(normalizedName).orElse(null);
    }

    private Player getPlayerWithStatsByNameAndType(String name, StatsType type) {
        Player player = getPlayerByName(name);
        if (player == null) return null;
        return getPlayerWithStatsByIdAndType(player.getId(), type);
    }

    @Override
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    private Player getPlayerWithStatsByIdAndType(Long id, StatsType type) {
        Optional<Player> maybePlayer = playerRepository.findById(id);
        return maybePlayer.map(player -> {
            PlayerStats stats = statsServiceImpl.getStatsByPlayerId(player.getId(), type);
            player.setStats(stats);
            return player;
        }).orElse(null);
    }

    private Player scrapePlayerWithName(String name, StatsType statsType) {
        String playerId = whoScoredServiceImpl.getIdFromFirstResult(name, () -> {
            throw new PlayerNotFoundException(name);
        });
        return scrapePlayerWithIdAndType(Long.valueOf(playerId), statsType);
    }

    @Override
    public Player scrapePlayerWithIdAndType(Long id, StatsType type) {
        TablePlayerStats tablePlayerStats = getTableStat(type, id);
        return saveOrUpdatePlayer(id, type, tablePlayerStats);
    }

    @Override
    public Player createPlayer(Long id, StatsType type, Team team) {
        TablePlayerStats tablePlayerStats = getTableStat(type, id);
        TableStat first = tablePlayerStats.getTableStats().getFirst();
        LocalDate lastTimeScrapped = LocalDate.now();
        return new Player(
                id,
                first.getName(),
                first.getPositions(),
                first.getDateOfBirth(),
                first.getNationality(),
                tablePlayerStats.getTableStats(),
                type,
                team,
                lastTimeScrapped
        );
    }

    private Player saveOrUpdatePlayer(Long id, StatsType type, TablePlayerStats tablePlayerStats) {
        Player player = playerRepository.findById(id).orElse(null);
        TableStat first = tablePlayerStats.getTableStats().getFirst();
        LocalDate lastTimeScrapped = LocalDate.now();

        if (player == null) {
            Long teamId = (long) first.getTeamId();
            Team team = teamRepository.findById(teamId).orElseGet(() -> {
                Team newTeam = new Team(teamId, first.getTeamName(), null, List.of(), List.of(), lastTimeScrapped);
                return teamRepository.save(newTeam);
            });
            player = new Player(id, first.getName(), first.getPositions(), first.getDateOfBirth(), first.getNationality(),
                    tablePlayerStats.getTableStats(), type, team, lastTimeScrapped);
        } else {
            PlayerStats stats = statsServiceImpl.getStatsByPlayerId(id, type);
            if (stats == null) {
                stats = type.newInstance(tablePlayerStats.getTableStats());
                stats.setPlayer(player);
            } else {
                stats.setResume(tablePlayerStats.getTableStats());
            }
            player.setStats(stats);
            player.setLastTimeScrapped(lastTimeScrapped);
        }
        return playerRepository.save(player);
    }

    private TablePlayerStats getTableStat(StatsType type, Long id) {
        String url = type.newInstance().getPlayerLink(id);
        String response = whoScoredServiceImpl.fetchJSONString(url);
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);
        return tablePlayerStats;
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }

    private Boolean hasToScrap(Player player, StatsType statsType) {
        if (player == null) return true;

        boolean needsScraping = player.hasToBeScrapped();
        if (needsScraping) return true;

        PlayerStats stats = player.getStats();
        return stats == null || !stats.getClass().equals(statsType.getStatsClass());
    }
}
