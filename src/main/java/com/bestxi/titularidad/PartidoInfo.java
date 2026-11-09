package com.bestxi.titularidad;

import java.time.LocalDateTime;

// Los datos del partido que se predice, tal como se envían a la web.
public record PartidoInfo(
        Integer jornada,
        String equipoLocal,
        String equipoVisitante,
        LocalDateTime fecha
) {
}
