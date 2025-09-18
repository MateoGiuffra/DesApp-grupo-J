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
    private Paging paging;
    private List<String> statColumns;


    public TablePlayerStats(String bodyText) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(bodyText);
            this.playerTableStats = mapper.readerForListOf(PlayerTableStat.class)
                    .readValue(root.get("playerTableStats").toString());
            this.paging = mapper.treeToValue(root.get("paging"), Paging.class);
            this.statColumns = mapper.readerForListOf(String.class)
                    .readValue(root.get("statColumns").toString());
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear bodyText", e);
        }
    }
}