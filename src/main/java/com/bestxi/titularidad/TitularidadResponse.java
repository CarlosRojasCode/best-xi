package com.bestxi.titularidad;

public record TitularidadResponse(
        Long jugadorId,
        String nombreJugador,
        String posicion,

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
        String explicacion
) {
}