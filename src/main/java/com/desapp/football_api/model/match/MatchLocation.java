package com.desapp.football_api.model.match;

import com.desapp.football_api.model.Team;

import java.util.List;

public enum MatchLocation {
    ALL {
        @Override
        public List<Match> filter(Team team) {
            return team.getMatches();
        }
    },
    HOME {
        @Override
        public List<Match> filter(Team team) {
            return team.getHomeMatches();
        }
    },
    AWAY {
        @Override
        public List<Match> filter(Team team) {
            return team.getAwayMatches();
        }
    };

    public abstract List<Match> filter(Team team);
}
