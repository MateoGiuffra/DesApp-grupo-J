package com.desapp.football_api.repository.stats;

import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    @Query("""
            SELECT s FROM PlayerStats s WHERE s.player.id = :playerId AND TYPE(s) = :type
            """)
    Optional<PlayerStats> findByPlayerIdAndType(Long playerId, Class type);

    @Query("""
            SELECT s FROM PlayerStats s JOIN s.player p WHERE p.team.id = :teamId AND TYPE(s) = :type
            """)
    List<PlayerStats> findByTeamIdAndType(Long teamId, Class type);
}
