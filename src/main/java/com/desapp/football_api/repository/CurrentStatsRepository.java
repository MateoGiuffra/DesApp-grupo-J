package com.desapp.football_api.repository;

import com.desapp.football_api.model.stats.CurrentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentStatsRepository extends JpaRepository<CurrentStats, Long> {
    java.util.Optional<CurrentStats> findByPlayerId(Long playerId);
}
