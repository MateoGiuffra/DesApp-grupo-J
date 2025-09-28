package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Long id;
    private String fullname;
    private String positions;
    private String dateOfBirth;
    private String nationality;

    private Stats stats;

    public Player(Map<String, Object> playerMap) {
        this.fullname = playerMap.containsKey("name") ? (String) playerMap.get("name") : null;
        this.positions = playerMap.containsKey("position") ? (String) playerMap.get("position") : null;
        this.dateOfBirth = playerMap.containsKey("dateOfBirth") ? (String) playerMap.get("dateOfBirth") : null;
        this.nationality = playerMap.containsKey("nationality") ? (String) playerMap.get("nationality") : null;
    }

    public Player(Long id, String name, String positions, String dateOfBirth, String nationality, String team, List<PlayerTableStat> playerTableStats, StatsType statsType) {
        this.id = id;
        this.fullname = name;
        this.positions = positions;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        statsType.setNewInstance(this, playerTableStats);
    }

    public Player(PlayerTableStat playerTableStat) {
        this.id = (long) playerTableStat.getPlayerId();
        this.fullname = playerTableStat.getName();
        this.positions = playerTableStat.getPositions();
        this.dateOfBirth = playerTableStat.getDateOfBirth();
        this.nationality = playerTableStat.getNationality();
    }

    public Integer getAssists() {
        return stats.getAssists();
    }
//
//    public Integer getGoals() {
//        return actualStats != null ? actualStats.getGoals() : null;
//    }
//
//    public Double getRating() {
//        return actualStats != null ? actualStats.getRating() : null;
//    }
//
//    public Integer getGames() {
//        return actualStats != null ? actualStats.getGames() : null;
//    }
//
//    public Integer getHistoricalAssists() {
//        return historicalStats != null ? historicalStats.getAssists() : null;
//    }
//
//    public Integer getHistoricalGoals() {
//        return historicalStats != null ? historicalStats.getGoals() : null;
//    }
//
//    public Double getHistoricalRating() {
//        return historicalStats != null ? historicalStats.getRating() : null;
//    }
//
//    public Integer getHistoricalGames() {
//        return historicalStats != null ? historicalStats.getGames() : null;
//    }

}
