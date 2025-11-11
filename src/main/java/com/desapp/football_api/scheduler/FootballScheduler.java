package com.desapp.football_api.scheduler;

import com.desapp.football_api.services.impl.TeamServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FootballScheduler {

    private final TeamServiceImpl teamServiceImpl;

    public FootballScheduler(TeamServiceImpl myService) {
        this.teamServiceImpl = myService;
    }

    // Every four hours
    @Scheduled(cron = "0 0 */4 * * *")
    public void runEvery4Hours() {
        teamServiceImpl.updateAllTeamsData();
    }
}
