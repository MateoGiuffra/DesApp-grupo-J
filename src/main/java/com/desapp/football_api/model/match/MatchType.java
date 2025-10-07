package com.desapp.football_api.model.match;

import com.desapp.football_api.model.Team;

import java.time.LocalDate;
import java.util.List;

public enum MatchType {
    ALL {
        @Override
        public List<Match> filter(Team team) {
            return team.getMatches();
        }
    },
    PAST {
        @Override
        public List<Match> filter(Team team) {
            return team.getPastMatches();
        }
    },
    UPCOMING {
        @Override
        public List<Match> filter(Team team) {
            return team.getUpcomingMatches();
        }
    };

    public abstract List<Match> filter(Team team);
}
