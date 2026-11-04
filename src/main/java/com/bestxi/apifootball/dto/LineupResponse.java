package com.bestxi.apifootball.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LineupResponse(List<LineupData> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LineupData(
            FixtureResponse.TeamRef team,
            CoachRef coach,
            String formation,
            List<PlayerSlot> startXI,
            List<PlayerSlot> substitutes
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoachRef(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerSlot(PlayerRef player) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerRef(Long id, String name, Integer number, String pos, String grid) {
    }
}
