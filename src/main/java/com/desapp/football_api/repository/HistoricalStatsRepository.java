package com.desapp.football_api.repository;

import com.desapp.football_api.model.stats.HistoricalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalStatsRepository extends JpaRepository<HistoricalStats, Long> {
    java.util.Optional<HistoricalStats> findByPlayerId(Long playerId);
}
