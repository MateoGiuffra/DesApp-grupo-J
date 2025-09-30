package com.desapp.football_api.repository;

import com.desapp.football_api.model.stats.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {
    @Query("""
            SELECT s FROM Stats s WHERE s.player.id = :playerId AND TYPE(s) = :type
            """)
    Optional<Stats> findByPlayerIdAndType(Long playerId, Class type);
}
