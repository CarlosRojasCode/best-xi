package com.bestxi.titularidad;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TitularidadController {

    private final TitularidadService titularidadService;

    public TitularidadController(TitularidadService titularidadService) {
        this.titularidadService = titularidadService;
    }

    // GET /jugadores/614/titularidad?ultimosPartidos=5&excluirPartidoId=1038211
    @GetMapping("/jugadores/{id}/titularidad")
    public TitularidadResponse obtenerTitularidad(
        @PathVariable("id") Long jugadorId,
        @RequestParam(defaultValue = "5") int ultimosPartidos,
        @RequestParam(required = false) Long excluirPartidoId,
        @RequestParam(required = false) Integer jornada
) {
    if (jornada != null) {
        return titularidadService.calcularTitularidadParaJornada(jugadorId, ultimosPartidos, jornada);
    }

    return titularidadService.calcularTitularidad(jugadorId, ultimosPartidos, excluirPartidoId);
}

    // GET /jugadores/668/titularidad/proximo-partido?ultimosPartidos=5
    @GetMapping("/jugadores/{id}/titularidad/proximo-partido")
    public TitularidadResponse obtenerTitularidadProximoPartido(
        @PathVariable("id") Long jugadorId,
        @RequestParam(defaultValue = "5") int ultimosPartidos
    ) {
        return titularidadService.calcularTitularidadProximoPartido(jugadorId, ultimosPartidos);
    }
}