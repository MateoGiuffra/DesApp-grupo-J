package com.desapp.football_api.model;

import com.desapp.football_api.model.player.SimplePlayer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Team {
    private String id;
    private List<SimplePlayer> squadList;

    public Team(Long id, Map<String, Object> body) {
        this.id = id.toString();
        this.squadList = new ArrayList<>();
        Object squadObj = body != null ? body.get("squad") : null;
        if (squadObj instanceof ArrayList) {
            for (Object playerObj : (ArrayList<?>) squadObj) {
                if (playerObj instanceof Map) {
                    SimplePlayer simplePlayer = new SimplePlayer((Map<String, Object>) playerObj);
                    this.squadList.add(simplePlayer);
                }
            }
        }


    }
}


