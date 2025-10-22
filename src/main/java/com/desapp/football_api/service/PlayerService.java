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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.desapp.football_api.utils.Normalizer.normalizeName;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PlayerService {

    private final WhoScoredService whoScoredService;
    private final PlayerRepository playerRepository;
    private final StatsService statsService;
    private final TeamRepository teamRepository;

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

        LocalDate lastTimeScrapped = LocalDate.now();
        Long teamId = (long) first.getTeamId();
        // Reuse existing Team if present to avoid clearing its squad (orphanRemoval on Team.squadList)
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            team = new Team(teamId, first.getTeamName(), null, List.of(), List.of(), lastTimeScrapped);
            teamRepository.save(team);
        } else {
            // Update minimal fields without altering players/matches collections
            team.setName(first.getTeamName());
            team.setLastTimeScrapped(lastTimeScrapped);
        }

        Player player = createPlayer(id, type, team);
        return playerRepository.save(player);
    }


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
        return player == null || player.getStats() == null || player.hasToBeScrapped();
    }

}
