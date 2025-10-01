package com.desapp.football_api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FootballScheduler {

    private final TeamService teamService;

    public FootballScheduler(TeamService myService) {
        this.teamService = myService;
    }

    // Cada 4 horas (cron: cada 4hs en el minuto 0, segundo 0)
    @Scheduled(cron = "0 * * * * *")
    public void runEvery4Hours() {
        //teamService.updateAllTeamsData();
        System.out.println("Scheduler running..." + System.currentTimeMillis());
    }
}
