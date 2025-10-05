package com.desapp.football_api.repository;

import com.desapp.football_api.model.stats.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {
    @Query("""
            SELECT s FROM Stats s WHERE s.player.id = :playerId AND TYPE(s) = :type
            """)
    Optional<Stats> findByPlayerIdAndType(Long playerId, Class type);

    @Query("""
            SELECT s FROM Stats s JOIN s.player p WHERE p.team.id = :teamId AND TYPE(s) = :type
            """)
    List<Stats> findByTeamIdAndType(Long teamId, Class type);
}
