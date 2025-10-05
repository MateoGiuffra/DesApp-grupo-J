package com.desapp.football_api.repository;

import com.desapp.football_api.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTeam_Id(Long teamId);
    void deleteByTeam_Id(Long teamId);
}
