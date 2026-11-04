package com.bestxi.apifootball.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InjuryResponse(List<InjuryData> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InjuryData(InjuryPlayerRef player, FixtureRef fixture) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InjuryPlayerRef(Long id, String name, String type, String reason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureRef(Long id) {
    }
}
