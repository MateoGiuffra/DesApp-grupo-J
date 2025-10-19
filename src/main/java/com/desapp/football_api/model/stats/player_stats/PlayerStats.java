package com.desapp.football_api.model.stats.player_stats;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;

@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Table(name = "player_stats")
@DiscriminatorColumn(name = "stats_type", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class PlayerStats extends Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    private Player player;
    private int assists;
    private int mins;

    public PlayerStats(TableStat tableStat) {
        super(tableStat);
        this.assists = tableStat.getAssistTotal();
        this.mins = tableStat.getMinsPlayed();
    }

    public PlayerStats(List<TableStat> tableStats) {
        super(tableStats);
    }

    @Override
    public void setExtraStats(List<TableStat> tableStats) {
        this.assists = tableStats.stream().mapToInt(TableStat::getAssistTotal).sum();
        this.mins = tableStats.stream().mapToInt(TableStat::getMinsPlayed).sum();

        List<TableStat> statsWithMinsForShots = tableStats.stream()
                .filter(stat -> stat.getMinsPlayed() > 0)
                .toList();
        double avgShots = statsWithMinsForShots.stream()
                .mapToDouble(TableStat::getShotsPerGame)
                .average()
                .orElse(0);
        this.shotsPerGame = statsWithMinsForShots.isEmpty() ? 0 : roundToTwoDecimals(avgShots);

        List<TableStat> statsWithMins = tableStats.stream()
                .filter(stat -> stat.getMinsPlayed() > 0)
                .toList();
        this.passSuccess = statsWithMins.isEmpty() ? 0 :
                roundToTwoDecimals(statsWithMins.stream().mapToDouble(TableStat::getPassSuccess).sum() / statsWithMins.size());

    }

}
