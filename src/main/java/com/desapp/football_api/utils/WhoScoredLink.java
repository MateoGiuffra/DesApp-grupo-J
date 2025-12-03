package com.desapp.football_api.utils;

public class WhoScoredLink {
    private WhoScoredLink() {
    }

    public static String getHistoricalPlayerLink(Long id) {
        return "https://es.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=false&playerId=" + id + "&teamIds=&matchId=&stageId=&tournamentOptions=&sortBy=seasonId&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
    }

    public static String getTeamLink(Long id) {
        return "https://es.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=&teamIds=" + id + "&matchId=&stageId=&tournamentOptions=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
    }

    public static String getCurrentPlayerLink(Long id) {
        return "https://es.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=" + id + "&teamIds=&matchId=&stageId=&tournamentOptions=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
    }

    public static String getTeamFixturesLink(Long teamId) {
        return "https://es.whoscored.com/teamsfeed/" + teamId + "/fixtures/?field=all&tournamentId=all&type=fixture";
    }

    public static String getTeamStatsLink(Long id) {
        return "https://es.whoscored.com/statisticsfeed/1/getteamstatistics?category=summaryteam&subcategory=all&statsAccumulationType=0&field=Overall&tournamentOptions=&timeOfTheGameStart=&timeOfTheGameEnd=&teamIds=" + id + "&stageId=&sortBy=Rating&sortAscending=&page=1&numberOfTeamsToPick=&isCurrent=true&formation=&incPens=&against=";
    }
}
