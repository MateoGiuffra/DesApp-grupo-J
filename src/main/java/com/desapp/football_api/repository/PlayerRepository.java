package com.desapp.football_api.repository;

import com.desapp.football_api.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByFullname(String fullname);

    @Query("""
            SELECT p FROM Player p WHERE p.team.id = :teamId
            """)
    List<Player> findByTeamId(@Param("teamId") Long teamId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE player p
            SET stats_id = (
                SELECT s.id FROM player_stats s
                WHERE s.player_id = p.id AND s.stats_type = :statsType
            )
            WHERE p.team_id = :teamId
            """, nativeQuery = true)
    int updatePlayersStatsReferenceForTeam(@Param("teamId") Long teamId, @Param("statsType") String statsType);
}
