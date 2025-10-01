package com.desapp.football_api.repository;

import com.desapp.football_api.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByFullname(String fullname);

    @Query("""
            SELECT p FROM Player p WHERE p.team.id = :teamId
            """)
    List<Player> findByTeamId(Long teamId);
}
