package com.desapp.football_api.model.match;

import com.desapp.football_api.model.Team;

import java.util.List;

public enum MatchType {
    ALL {
        @Override
        public List<Match> filter(Team team) {
            return team.getMatches();
        }

        @Override
        public Boolean isAfter() {
            return null;
        }
    },
    PAST {
        @Override
        public List<Match> filter(Team team) {
            return team.getPastMatches();
        }

        @Override
        public Boolean isAfter() {
            return false;
        }
    },
    UPCOMING {
        @Override
        public List<Match> filter(Team team) {
            return team.getUpcomingMatches();
        }

        @Override
        public Boolean isAfter() {
            return true;
        }
    };

    public abstract List<Match> filter(Team team);

    public abstract Boolean isAfter();
}
