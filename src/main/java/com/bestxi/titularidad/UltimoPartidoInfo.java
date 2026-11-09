package com.bestxi.titularidad;

// Un partido reciente del jugador, tal como se envía a la web.
public record UltimoPartidoInfo(
        Integer jornada,
        Boolean titular
) {
}
