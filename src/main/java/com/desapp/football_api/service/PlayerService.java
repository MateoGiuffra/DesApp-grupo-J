package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.WhoScoredHelper;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.desapp.football_api.model.WhoScoredHelper.calculateBirthDateByAge;
import static com.desapp.football_api.model.WhoScoredHelper.getCountryNameFromCode;

@Service
public class PlayerService {

    @Autowired
    private WhoScoredService whoScoredService;

    public Player scrapPlayerWithName(String name) throws IOException, InterruptedException {
        String playerId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new PlayerNotFoundException(name);
        });
        return scrapPlayerWithId(Long.valueOf(playerId));
    }

    public Player scrapPlayerWithId(Long id) throws java.io.IOException, InterruptedException {
        String url = "https://es.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=" + id + "&teamIds=&matchId=&stageId=&tournamentOptions=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
        String response = whoScoredService.fetchJSONString(url);
        return createPlayerFromJSON(response, id);
    }

    public Player createPlayerFromJSON(String response, Long id) {
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);

        List<PlayerTableStat> playerTableStats = tablePlayerStats.getPlayerTableStats();
        PlayerTableStat first = playerTableStats.getFirst();

        String fullname = first.getName();
        String dateOfBirth = calculateBirthDateByAge(first.getAge());
        String nationality = getCountryNameFromCode(first.getRegionCode());
        String positions = WhoScoredHelper.parsePlayedPositions(first.getPlayedPositions());
        String team = first.getTeamName();
        return new Player(id, fullname, positions, dateOfBirth, nationality, team, playerTableStats);
    }

    private void validatePlayerExists(TablePlayerStats tablePlayerStats, Long id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }


}