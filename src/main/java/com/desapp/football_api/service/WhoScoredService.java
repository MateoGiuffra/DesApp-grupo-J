package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.Player;
import com.desapp.football_api.model.WhoScoredHelper;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.model.table_player_stats.TablePlayerStats;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.desapp.football_api.model.WhoScoredHelper.calculateBirthDateByAge;
import static com.desapp.football_api.model.WhoScoredHelper.getCountryNameFromCode;

@Service
public class WhoScoredService {
    private final static String URL = "https://es.whoscored.com/players/";

    public Player scrapPlayerWithId(String id) throws java.io.IOException, InterruptedException {
        String url = "https://es.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=" + id + "&teamIds=&matchId=&stageId=&tournamentOptions=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
        String response = fetchPlayerJSON(url);
        return createPlayerFromJSON(response, id);
    }

    private String fetchPlayerJSON(String url) throws java.io.IOException, InterruptedException {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .header("Referer", "https://es.whoscored.com/")
                .header("Cookie", "_fbp=fb.1.1758064465942.476798853467207590; _xpid=6325398004; _xpkey=kCSpXKfdThqha20bNjE_uvq4T__NKd9J; _adm-gpp=DBAA; _gid=GA1.2.1444691027.1758064509; ...") // recorta la cookie si es necesario
                .GET()
                .build();
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    public Player createPlayerFromJSON(String response, String id) {
        TablePlayerStats tablePlayerStats = new TablePlayerStats(response);
        validatePlayerExists(tablePlayerStats, id);

        List<PlayerTableStat> playerTableStats = tablePlayerStats.getPlayerTableStats();
        PlayerTableStat first = playerTableStats.getFirst();

        String fullname = first.getName();
        String dateOfBirth = calculateBirthDateByAge(first.getAge());
        String nationality = getCountryNameFromCode(first.getRegionCode());
        String positions = WhoScoredHelper.parsePlayedPositions(first.getPlayedPositions());
        String team = first.getTeamName();
        return new Player(fullname, positions, dateOfBirth, nationality, team, playerTableStats);
    }


    private void validatePlayerExists(TablePlayerStats tablePlayerStats, String id) {
        if (!tablePlayerStats.playerExists()) {
            throw new PlayerNotFoundException(id);
        }
    }


}