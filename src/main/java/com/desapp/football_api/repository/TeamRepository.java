package com.desapp.football_api.repository;

import com.desapp.football_api.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT t.id FROM Team t")
    List<Long> findAllIds();


}
