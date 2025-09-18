package com.desapp.football_api.model.table_player_stats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Paging {
    private int currentPage;
    private int totalPages;
    private int resultsPerPage;
    private int totalResults;
    private int firstRecordIndex;
    private int lastRecordIndex;
}