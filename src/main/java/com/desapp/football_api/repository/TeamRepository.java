package com.desapp.football_api.repository;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    @Query("""
                SELECT t FROM Team t
                LEFT JOIN FETCH t.squadList p
                WHERE t.id = :id
            """)
    Team findByIdWithPlayers(Long id);

    @Query("""
                SELECT t FROM Team t
                LEFT JOIN FETCH t.matches m
                WHERE t.id = :id
            """)
    Team findByIdWithMatches(Long id);

    @Query("SELECT t.id FROM Team t")
    List<Long> findAllIds();

    @Query("""
                            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                            FROM Team t
                            LEFT JOIN t.matches m
                            WHERE t.id = :teamId AND m IS NOT NULL
            """)
    boolean existsByIdWithMatches(Long teamId);

    // tests

    @Query("""
                SELECT m FROM Match m
                WHERE (
                        (:isAtHome IS NULL)
                     OR (:isAtHome = true AND m.homeTeamId = :teamId)
                     OR (:isAtHome = false AND m.awayTeamId = :teamId)
                  )
                  AND (
                        (:isAfter IS NULL)
                     OR (:isAfter = true AND m.date > :date)
                     OR (:isAfter = false AND m.date < :date)
                  )
            """)
    List<Match> findMatchesByTypeAndLocation(
            @Param("teamId") Long teamId,
            @Param("date") LocalDate date,
            @Param("isAfter") Boolean isAfter,
            @Param("isAtHome") Boolean isAtHome
    );


}
