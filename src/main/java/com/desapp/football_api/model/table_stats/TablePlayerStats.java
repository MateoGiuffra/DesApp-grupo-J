package com.desapp.football_api.model.table_stats;

import com.desapp.football_api.exceptions.who_scored.WhoScoredServiceUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
@ToString
public class TablePlayerStats {
    private List<TableStat> tableStats;
    private static final Logger logger = LoggerFactory.getLogger(TablePlayerStats.class);
    public TablePlayerStats(String bodyText) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(bodyText);
            this.tableStats = mapper.readerForListOf(TableStat.class).readValue(root.get("playerTableStats").toString());
        } catch (Exception e) {
            System.out.println("TablePlayerStats constructor error:   " + e.getMessage());
            throw new WhoScoredServiceUnavailableException();
        }
    }

    public boolean playerExists() {
        return !this.tableStats.isEmpty();
    }

}