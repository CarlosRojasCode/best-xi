package com.bestxi.apifootball.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // ignora cualquier campo del JSON que no nos interese
public record FixtureResponse(List<FixtureData> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureData(FixtureInfo fixture, Teams teams, LeagueInfo league) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureInfo(Long id, String date) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Teams(TeamRef home, TeamRef away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamRef(Long id, String name, String logo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfo(String round) {
    }
}
