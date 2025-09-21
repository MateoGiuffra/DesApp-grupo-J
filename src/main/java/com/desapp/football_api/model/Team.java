package com.desapp.football_api.model;

import com.desapp.football_api.model.player.SimplePlayer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Team {
    private String id;
    private List<SimplePlayer> squad;

    public Team(Long id, Map body) {
        this.id = id.toString();
        this.squad = new ArrayList<>();
        Object squad = body != null ? body.get("squad") : null;
        if (squad instanceof ArrayList) {
            for (Object playerObj : (ArrayList<?>) squad) {
                if (playerObj instanceof Map) {
                    SimplePlayer simplePlayer = new SimplePlayer((Map<String, Object>) playerObj);
                    this.squad.add(simplePlayer);
                }
            }
        }


    }
}


