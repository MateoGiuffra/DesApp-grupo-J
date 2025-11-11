package com.desapp.football_api.services;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;

public interface PlayerService {

    Player getPlayerByIdAndType(Long id, StatsType type);

    Player getPlayerByNameAndType(String name, StatsType type);

    Player getPlayerByName(String name);

    Player getPlayerById(Long id);

    Player scrapePlayerWithIdAndType(Long id, StatsType type);

    Player createPlayer(Long id, StatsType type, Team team);


}
