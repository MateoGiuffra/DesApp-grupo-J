package com.desapp.football_api.scheduler;

import com.desapp.football_api.service.TeamService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FootballScheduler {

    private final TeamService teamService;

    public FootballScheduler(TeamService myService) {
        this.teamService = myService;
    }

    // Every four hours
    @Scheduled(cron = "0 0 */4 * * *")
    public void runEvery4Hours() {
        teamService.updateAllTeamsData();
    }
}
