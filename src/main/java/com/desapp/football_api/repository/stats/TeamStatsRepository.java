package com.desapp.football_api.repository.stats;

import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {

}
