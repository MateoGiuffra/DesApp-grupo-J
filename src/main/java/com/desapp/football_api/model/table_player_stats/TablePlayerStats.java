package com.desapp.football_api.model.table_player_stats;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TablePlayerStats {
    private List<PlayerTableStat> playerTableStats;

    public TablePlayerStats(String bodyText) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(bodyText);
            this.playerTableStats = mapper.readerForListOf(PlayerTableStat.class).readValue(root.get("playerTableStats").toString());
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing bodyText", e);
        }
    }

    public boolean playerExists() {
        return !this.playerTableStats.isEmpty();
    }

}