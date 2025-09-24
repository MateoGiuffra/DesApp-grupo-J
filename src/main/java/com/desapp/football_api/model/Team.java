package com.desapp.football_api.model;

import com.desapp.football_api.exceptions.who_scored.WhoScoredServiceUnavailableException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Team {
    private Long id;
    private List<Player> squadList;


    public Team(Long id, String body) {
        this.id = id;
        this.squadList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(body);
            List<PlayerTableStat> playerTableStatList = mapper.readerForListOf(PlayerTableStat.class).readValue(root.get("playerTableStats").toString());
            playerTableStatList.forEach((playerTableStat -> {
                Player player = new Player(playerTableStat);
                squadList.add(player);
            }));
        } catch (Exception e) {
            throw new WhoScoredServiceUnavailableException();
        }
    }

}


