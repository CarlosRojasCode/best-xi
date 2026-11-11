package com.bestxi.titularidad;

public record TitularidadResponse(
        Long jugadorId,
        String nombreJugador,
        String posicion,

        // Datos para la ficha visual del jugador
        Integer dorsal,
        String equipoNombre,
        String escudoUrl,

        // Señal 1: frecuencia en los últimos N partidos
        int partidosAnalizados,
        int titularesRecientes,
        int frecuenciaReciente, // porcentaje 0-100

        // Señal 2: rendimiento bajo el entrenador actual
        Integer partidosConEntrenadorActual,
        Integer frecuenciaConEntrenadorActual, // null si no hay datos suficientes

        // Señal 3: rendimiento según casa/fuera
        Integer partidosCasa,
        Integer frecuenciaCasa,
        Integer partidosFuera,
        Integer frecuenciaFuera,

        // Señal 4: forma muy reciente (últimos 3 partidos, fija, para detectar bajones de forma)
        Integer frecuenciaUltimos3,

        // El número principal a mostrar, combinando las señales anteriores
        int porcentajeFinal,

        // El "por qué" en texto, para humanos
        String explicacion,

        // El partido que se está prediciendo (null si no se predice ninguno concreto)
        PartidoInfo partido,

        // Sus últimos partidos, del más reciente al más antiguo
        java.util.List<UltimoPartidoInfo> ultimosPartidos,

        // Ausencias justificadas agrupadas por motivo
        java.util.List<AusenciaInfo> ausencias
) {
}