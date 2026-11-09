package com.bestxi.titularidad;

// Un motivo de ausencia y cuántos partidos se perdió el jugador por él.
public record AusenciaInfo(
        String motivo,
        Integer partidos
) {
}
