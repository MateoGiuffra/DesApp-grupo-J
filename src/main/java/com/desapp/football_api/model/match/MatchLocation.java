package com.desapp.football_api.model.match;

import com.desapp.football_api.model.Team;

import java.util.List;

public enum MatchLocation {
    ALL {
        @Override
        public List<Match> filter(Team team) {
            return team.getMatches();
        }

        @Override
        public Boolean isAtHome() {
            return null;
        }
    },
    HOME {
        @Override
        public List<Match> filter(Team team) {
            return team.getHomeMatches();
        }

        @Override
        public Boolean isAtHome() {
            return true;
        }
    },
    AWAY {
        @Override
        public List<Match> filter(Team team) {
            return team.getAwayMatches();
        }

        @Override
        public Boolean isAtHome() {
            return false;
        }
    };

    public abstract List<Match> filter(Team team);

    public abstract Boolean isAtHome();
}
