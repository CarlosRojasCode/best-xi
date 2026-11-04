package com.bestxi.apifootball;

import com.bestxi.apifootball.dto.FixtureResponse;
import com.bestxi.apifootball.dto.InjuryResponse;
import com.bestxi.apifootball.dto.LineupResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiFootballClient {

    private final RestClient restClient;

    public ApiFootballClient(RestClient apiFootballRestClient) {
        this.restClient = apiFootballRestClient;
    }

    // Equivale a: GET /fixtures?league=140&season=2023&team=543
    public FixtureResponse obtenerPartidos(int liga, int temporada, long equipoId) {
        return restClient.get()
                .uri("/fixtures?league={liga}&season={temporada}&team={equipoId}", liga, temporada, equipoId)
                .retrieve()
                .body(FixtureResponse.class);
    }

    // Equivale a: GET /fixtures/lineups?fixture=1038025
    public LineupResponse obtenerAlineaciones(long partidoId) {
        return restClient.get()
                .uri("/fixtures/lineups?fixture={partidoId}", partidoId)
                .retrieve()
                .body(LineupResponse.class);
    }

    // Equivale a: GET /injuries?league=140&season=2023&team=543
    public InjuryResponse obtenerIncidencias(int liga, int temporada, long equipoId) {
        return restClient.get()
                .uri("/injuries?league={liga}&season={temporada}&team={equipoId}", liga, temporada, equipoId)
                .retrieve()
                .body(InjuryResponse.class);
    }
}
