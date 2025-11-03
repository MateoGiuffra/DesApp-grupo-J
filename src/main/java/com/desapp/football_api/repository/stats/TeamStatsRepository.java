package com.desapp.football_api.repository.stats;

import com.desapp.football_api.model.stats.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TeamStatsRepository extends JpaRepository<TeamStats, Long> {

}
